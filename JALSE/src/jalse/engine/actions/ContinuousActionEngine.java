package jalse.engine.actions;

import static jalse.engine.EngineState.IN_TICK;
import static jalse.engine.EngineState.IN_WAIT;
import static jalse.engine.EngineState.PAUSED;
import static jalse.engine.EngineState.STOPPED;
import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.engine.AbstractEngineBindings;
import jalse.engine.AbstractTickInfo;
import jalse.engine.EngineBindings;
import jalse.engine.EngineState;
import jalse.engine.TickInfo;
import jalse.listeners.EngineListener;
import jalse.listeners.ListenerSet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Logger;

/**
 * This is a self-managed continuously ticking {@link ActionEngine} implementation. This engine will
 * use up to the number of threads specified as workers (depending on number of {@link Action}
 * waiting to be executed per tick). An extra control thread will be added to the engine for work
 * maintenance. The engine will not be active upon creation it must be started with {@link #tick()}.
 *
 * @author Elliot Ford
 *
 * @see #SPIN_YIELD_THRESHOLD
 * @see #TERMINATION_TIMEOUT
 */
public final class ContinuousActionEngine implements ActionEngine {

    private class ContinuousEngineBindings extends AbstractEngineBindings {

	@Override
	protected void clearTickBindings() {
	    super.clearTickBindings();
	}
    }

    private class ContinuousTickInfo extends AbstractTickInfo {

	public ContinuousTickInfo(final int tps) {
	    super(tps);
	}

	@Override
	public void incrementTicks() {
	    super.incrementTicks();
	}

	@Override
	public void setCurrentTPS(final int currentTps) {
	    super.setCurrentTPS(currentTps);
	}

	@Override
	public void setDeltaAsNanos(final long delta) {
	    super.setDeltaAsNanos(delta);
	}
    }

    private class JobExecutor extends ThreadPoolExecutor {

	private final AtomicInteger active;

	@SuppressWarnings("serial")
	private JobExecutor(final int totalThreads) {
	    super(0, totalThreads, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>() {

		@Override
		public boolean offer(final Runnable o) {
		    return jobExecutor.getActiveCount() + super.size() < jobExecutor.getPoolSize() && super.offer(o);
		}
	    });

	    active = new AtomicInteger();

	    setRejectedExecutionHandler((r, e) -> {
		try {
		    getQueue().put(r);
		} catch (final InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	    });
	}

	@Override
	protected void afterExecute(final Runnable r, final Throwable t) {
	    active.decrementAndGet();
	    jobPhaser.arriveAndDeregister();
	    synchronized (jobQueue) {
		final JobTask<?> task = (JobTask<?>) r;
		final ActionJob<?> job = task.job;
		final ActionContext<?> context = job.getContext();

		jobFutures.remove(context.getID());
		if (!task.isCancelled() && context.isPeriodic()) {
		    job.estimateForReschedule();
		    jobQueue.add(job);
		}
	    }
	}

	@Override
	protected void beforeExecute(final Thread t, final Runnable r) {
	    active.incrementAndGet();
	}

	@Override
	public int getActiveCount() {
	    return active.get();
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
	    return new JobTask<>(runnable, value);
	}
    }

    private class JobTask<V> extends FutureTask<V> {

	private final ActionJob<?> job;

	public JobTask(final Runnable runnable, final V result) {
	    super(runnable, result);
	    job = (ActionJob<?>) runnable;
	}
    }

    private static final Logger logger = Logger.getLogger(ContinuousActionEngine.class.getName());

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    private static final Set<EngineState> START_TICK_EXPECTED = EnumSet.of(PAUSED);
    private static final Set<EngineState> CONTINUE_TICK_EXPECTED = EnumSet.of(IN_WAIT);
    private static final Set<EngineState> WAIT_EXPECTED = EnumSet.of(IN_TICK);
    private static final Set<EngineState> PAUSE_EXPECTED = EnumSet.of(IN_WAIT, IN_TICK);
    private static final Set<EngineState> STOP_EXPECTED = EnumSet.of(IN_TICK, IN_WAIT, PAUSED);

    /**
     * When Java sleeps it can sometimes be inaccurate, the engine will sleep up to this threshold
     * then spin yield until the desired wake time (configured via
     * {@code jalse.actions.engine.termination_timeout} system property).
     */
    public static final long SPIN_YIELD_THRESHOLD;

    /**
     * How long the engine will wait until it times out and interrupts running threads on shutdown
     * (configured via {@code jalse.actions.engine.spin_yield_threshold} system property).
     */
    public static final long TERMINATION_TIMEOUT;

    static {
	final String syt = System.getProperty("jalse.actions.engine.spin_yield_threshold");
	SPIN_YIELD_THRESHOLD = syt != null && syt.length() > 0 ? Long.valueOf(syt) : TimeUnit.MILLISECONDS.toNanos(10);

	final String tt = System.getProperty("jalse.actions.engine.termination_timeout");
	TERMINATION_TIMEOUT = tt != null && tt.length() > 0 ? Long.valueOf(tt) : 2 * SECOND;
    }

    private static void parkNanos(final long end) {
	long timeLeft;
	while ((timeLeft = end - System.nanoTime()) > 0) {
	    if (timeLeft > SPIN_YIELD_THRESHOLD) {
		LockSupport.parkNanos(timeLeft - SPIN_YIELD_THRESHOLD);
	    } else {
		Thread.yield();
	    }
	}
    }

    private final ExecutorService controlExecutor;
    private final ThreadPoolExecutor jobExecutor;
    private final ActionJobQueue jobQueue;
    private final Phaser jobPhaser;
    private final Map<UUID, Future<?>> jobFutures;
    private final AtomicReference<ActionJob<?>> firstJob;
    private final AtomicReference<ActionJob<?>> lastJob;
    private final ContinuousTickInfo tickInfo;
    private final ContinuousEngineBindings bindings;
    private final ListenerSet<EngineListener> listeners;
    private final StampedLock stateLock;
    private EngineState state;

    /**
     * Creates a new instance of continuous action engine.
     *
     * @param tps
     *            Maximum ticks per second to run at.
     * @param totalThreads
     *            Maximum number of threads to use up to for performing actions.
     */
    public ContinuousActionEngine(final int tps, final int totalThreads) {
	if (tps <= 0 || totalThreads <= 0) {
	    throw new IllegalArgumentException();
	}

	controlExecutor = Executors.newSingleThreadExecutor();
	jobExecutor = new JobExecutor(totalThreads + 1);
	jobQueue = new ActionJobQueue();
	jobPhaser = new Phaser();
	jobFutures = new HashMap<>();
	firstJob = new AtomicReference<>();
	lastJob = new AtomicReference<>();
	tickInfo = new ContinuousTickInfo(tps);
	bindings = new ContinuousEngineBindings();
	listeners = new ListenerSet<>(EngineListener.class);
	stateLock = new StampedLock();
	state = EngineState.PAUSED;
    }

    @Override
    public boolean addEngineListener(final EngineListener listener) {
	return listeners.add(listener);
    }

    @Override
    public boolean cancel(final UUID action) {
	validateNotStopped();
	Objects.requireNonNull(action);

	synchronized (jobQueue) {
	    final Future<?> f = jobFutures.get(action);
	    if (f != null && !f.isDone()) {
		return f.cancel(false);
	    }

	    return jobQueue.removeJob(action);
	}
    }

    private <T> ActionJob<T> createJob(final Action<T> action, final T actor, final long initialDelay, final long period) {
	final ActionContext<T> context = new DefaultActionContext<>(this, UUID.randomUUID(), actor, period);
	return new ActionJob<>(action, context, initialDelay);
    }

    @Override
    public EngineBindings getBindings() {
	return bindings;
    }

    @Override
    public Set<? extends EngineListener> getEngineListeners() {
	return Collections.unmodifiableSet(listeners);
    }

    @Override
    public EngineState getState() {
	long stamp = stateLock.tryOptimisticRead();

	/*
	 * Try optimistic.
	 */
	if (stateLock.validate(stamp)) {
	    return state;
	}

	/*
	 * Get read lock.
	 */
	stamp = stateLock.readLock();
	final EngineState result = state;
	stateLock.unlockRead(stamp);
	return result;
    }

    @Override
    public TickInfo getTickInfo() {
	return tickInfo;
    }

    @Override
    public boolean isActive(final UUID action) {
	validateNotStopped();
	Objects.requireNonNull(action);
	synchronized (jobQueue) {
	    final Future<?> f = jobFutures.get(action);
	    return f != null ? !f.isDone() : jobQueue.containsJob(action);
	}
    }

    private Runnable newControl() {
	return () -> {
	    long lastStart = System.nanoTime(), lastTpsCalc = System.nanoTime();
	    int currentTps = 0;
	    jobPhaser.register();

	    /*
	     * Main work loop.
	     */
	    while (tryChangeState(IN_TICK, CONTINUE_TICK_EXPECTED)) {
		final long start = System.nanoTime(), estimatedEnd = start + tickInfo.getIntervalAsNanos();
		tickInfo.setDeltaAsNanos(start - lastStart);
		lastStart = start;

		/*
		 * Calculate current TPS.
		 */
		if (start - lastTpsCalc >= SECOND) {
		    tickInfo.setCurrentTPS(currentTps);
		    lastTpsCalc = start;
		    currentTps = 0;
		}

		/*
		 * First job.
		 */
		final ActionJob<?> first = firstJob.get();
		if (first != null) {
		    first.run();
		}

		/*
		 * Schedule work for this tick.
		 */
		synchronized (jobQueue) {
		    for (final ActionJob<?> job : jobQueue.drainBeforeEstimated(estimatedEnd)) {
			jobPhaser.register();
			jobFutures.put(job.getContext().getID(), jobExecutor.submit(job));
		    }
		}
		jobPhaser.arriveAndAwaitAdvance();

		/*
		 * Last job.
		 */
		final ActionJob<?> last = lastJob.get();
		if (last != null) {
		    last.run();
		}

		bindings.clearTickBindings(); // Clear tick bindings.

		/*
		 * Increment tick statistics.
		 */
		tickInfo.incrementTicks();
		currentTps++;

		/*
		 * Wait for end of tick.
		 */
		if (tryChangeState(IN_WAIT, WAIT_EXPECTED)) {
		    parkNanos(estimatedEnd);
		}
	    }
	    jobPhaser.arriveAndDeregister();
	};
    }

    /**
     * Pauses the engine ticking.
     */
    @Override
    public void pause() {
	tryChangeState(PAUSED, PAUSE_EXPECTED);
    }

    @Override
    public boolean removeEngineListener(final EngineListener listener) {
	return listeners.remove(listener);
    }

    @Override
    public <T> UUID scheduleAction(final Action<T> action, final T actor, final long initialDelay, final long period,
	    final TimeUnit unit) {
	validateNotStopped();
	if (initialDelay < 0 || period < 0) {
	    throw new IllegalArgumentException();
	}

	final ActionJob<T> job = createJob(action, actor, unit.toNanos(initialDelay), unit.toNanos(period));
	synchronized (jobQueue) {
	    jobQueue.add(job);
	}

	return job.getContext().getID();
    }

    @Override
    public <T> void setFirstAction(final Action<T> action, final T actor) {
	validateNotStopped();
	firstJob.set(createJob(action, actor, 0, tickInfo.getIntervalAsNanos()));
    }

    @Override
    public <T> void setLastAction(final Action<T> action, final T actor) {
	validateNotStopped();
	lastJob.set(createJob(action, actor, 0, tickInfo.getIntervalAsNanos()));
    }

    @Override
    public void stop() {
	tryChangeState(STOPPED, STOP_EXPECTED);

	synchronized (jobQueue) {
	    jobQueue.clear();
	}

	controlExecutor.shutdown();
	jobExecutor.shutdown();

	try {
	    jobExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.NANOSECONDS);
	} catch (final InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
    }

    @Override
    public void tick() {
	if (tryChangeState(IN_WAIT, START_TICK_EXPECTED)) {
	    controlExecutor.submit(newControl());
	}
    }

    private boolean tryChangeState(final EngineState newState, final Set<EngineState> expectedPrevious) {
	long stamp = stateLock.tryOptimisticRead();

	/*
	 * Try optimistic.
	 */
	if (stateLock.validate(stamp)) {
	    if (state == STOPPED) { // Already shutdown.
		throwRE(ENGINE_SHUTDOWN);
	    }
	    if (!expectedPrevious.contains(state)) { // Doesn't match expected.
		return false;
	    }
	}

	/*
	 * Get write lock.
	 */
	stamp = stateLock.tryConvertToWriteLock(stamp);
	if (stamp == 0L) {
	    stamp = stateLock.writeLock();
	}

	/*
	 * Check should change.
	 */
	if (state == STOPPED) { // Already shutdown.
	    stateLock.unlockWrite(stamp);
	    throwRE(ENGINE_SHUTDOWN);
	}
	if (!expectedPrevious.contains(state)) { // Doesn't match expected.
	    stateLock.unlockWrite(stamp);
	    return false;
	}

	final boolean changed = state != newState;
	final EngineState oldState = state;

	/*
	 * Change state.
	 */
	if (changed) {
	    state = newState;
	    logger.fine(String.format("Engine state changed: %s -> %s", oldState, state));
	}

	/*
	 * Release lock.
	 */
	stateLock.unlockWrite(stamp);

	/*
	 * Trigger listeners.
	 */
	if (changed) {
	    listeners.getProxy().stateChanged(newState, oldState);
	}

	return changed;
    }

    private void validateNotStopped() {
	if (getState() == STOPPED) {
	    throwRE(ENGINE_SHUTDOWN);
	}
    }
}
