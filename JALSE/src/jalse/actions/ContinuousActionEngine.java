package jalse.actions;

import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.listeners.EngineListener;
import jalse.listeners.ListenerSet;
import jalse.misc.AbstractIdentifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
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
public class ContinuousActionEngine implements ActionEngine {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class Worker extends AbstractIdentifiable implements Runnable, Comparable<Worker> {

	private final Action action;
	private final Object actor;
	private long estimated;
	private final long period;

	private Worker(final UUID key, final Action action, final Object actor, final long initialDelay,
		final long period) {
	    super(key);
	    this.period = period;
	    this.action = action;
	    this.actor = actor;
	    estimated = System.nanoTime() + initialDelay;
	}

	@Override
	public int compareTo(final Worker o) {
	    return estimated < o.estimated ? -1 : estimated == o.estimated ? 0 : 1;
	}

	@Override
	public void run() {
	    try {
		action.perform(actor, tick);
	    } catch (final Exception e) {
		if (e instanceof InterruptedException) {
		    Thread.currentThread().interrupt();
		}

		logger.log(Level.WARNING, "Error performing action", e);
	    } finally {
		phaser.arriveAndDeregister();
	    }
	}
    }

    private class WorkExecutor extends ThreadPoolExecutor {

	private final AtomicInteger active;

	@SuppressWarnings("serial")
	private WorkExecutor(final int totalThreads) {
	    super(0, totalThreads, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>() {

		@Override
		public boolean offer(final Runnable o) {
		    return executor.getActiveCount() + super.size() < executor.getPoolSize() && super.offer(o);
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

	    final WorkTask<?> wt = (WorkTask<?>) r;

	    if (wt.runnable instanceof Worker) {
		final Worker w = (Worker) wt.runnable;
		synchronized (work) {
		    futures.remove(w.getID());
		    if (!wt.isCancelled() && w.period > 0L) {
			w.estimated = System.nanoTime() + w.period;
			work.add(w);
		    }
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
	    return new WorkTask<>(runnable, value);
	}
    }

    private class WorkTask<V> extends FutureTask<V> {

	private final Runnable runnable;

	public WorkTask(final Runnable runnable, final V result) {
	    super(runnable, result);
	    this.runnable = runnable;
	}
    }

    private static final Logger logger = Logger.getLogger(ContinuousActionEngine.class.getName());

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

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

    private final ThreadPoolExecutor executor;
    private final ActionWithActor first;
    private final Map<UUID, Future<?>> futures;
    private final ActionWithActor last;
    private final StampedLock lock;
    private final Phaser phaser;
    private final AtomicBoolean running;
    private final DefaultTickInfo tick;
    private final Queue<Worker> work;
    private final ListenerSet<EngineListener> listeners;
    private int state;

    /**
     * Creates a new instance of continuous action engine.
     *
     * @param tps
     *            Maximum ticks per second to run at.
     * @param totalThreads
     *            Maximum number of threads to use up to for performing actions.
     */
    public ContinuousActionEngine(final int tps, final int totalThreads) {
	if (tps <= 0 || totalThreads <= 0 || totalThreads == Integer.MAX_VALUE) {
	    throw new IllegalArgumentException();
	}

	executor = new WorkExecutor(totalThreads + 1);
	work = new PriorityQueue<>();
	phaser = new Phaser();
	futures = new HashMap<>();
	running = new AtomicBoolean();
	lock = new StampedLock();
	tick = new DefaultTickInfo(tps);
	first = new ActionWithActor();
	last = new ActionWithActor();
	listeners = new ListenerSet<>(EngineListener.class);
	state = PAUSED;
    }

    @Override
    public boolean addEngineListener(final EngineListener listener) {
	return listeners.add(listener);
    }

    @Override
    public boolean cancel(final UUID action) {
	validateActive();
	Objects.requireNonNull(action);

	synchronized (work) {
	    final Future<?> f = futures.get(action);
	    if (f != null && !f.isDone()) {
		return f.cancel(false);
	    }

	    return work.remove(new AbstractIdentifiable(action) {});
	}
    }

    private void changeState(final int state) {
	final long stamp = lock.writeLock();

	boolean allow = false;
	RuntimeException e = null;

	switch (this.state) {
	case IN_TICK:
	case IN_WAIT:
	    allow = state == IN_TICK || state == IN_WAIT || state == PAUSED || state == STOPPED;
	    break;
	case PAUSED:
	    allow = state == IN_TICK || state == STOPPED;
	    break;
	case STOPPED:
	    e = ENGINE_SHUTDOWN.get();
	    break;
	}

	if (allow) {
	    final int oldState = this.state;
	    this.state = state;

	    listeners.getProxy().stateChanged(state, oldState);
	}

	lock.unlockWrite(stamp);

	if (e != null) {
	    throw e;
	}
    }

    private Runnable control() {
	return () -> {
	    long lastStart = System.nanoTime(), lastTpsCalc = System.nanoTime();
	    int currentTps = 0;
	    phaser.register();

	    while (running.get()) {
		changeState(IN_TICK);
		final long start = System.nanoTime(), estimatedEnd = start + tick.getIntervalAsNanos();
		tick.setDelta(start - lastStart);

		if (start - lastTpsCalc >= SECOND) {
		    tick.setCurrentTPS(currentTps);
		    lastTpsCalc = start;
		    currentTps = 0;
		}

		first.perform(tick);

		synchronized (work) {
		    for (;;) {
			final Worker w = work.peek();
			if (w == null || w.estimated >= estimatedEnd) {
			    break;
			}
			work.remove();
			phaser.register();
			futures.put(w.getID(), executor.submit(w));
		    }
		}

		phaser.arriveAndAwaitAdvance();
		last.perform(tick);
		tick.incrementTicks();
		currentTps++;

		changeState(IN_WAIT);
		parkNanos(estimatedEnd);
		lastStart = start;
	    }
	    phaser.arriveAndDeregister();
	};
    }

    @Override
    public Set<? extends EngineListener> getEngineListeners() {
	return Collections.unmodifiableSet(listeners);
    }

    @Override
    public int getState() {
	long stamp = lock.tryOptimisticRead();

	if (lock.validate(stamp)) {
	    return state;
	}

	stamp = lock.readLock();
	final int result = state;
	lock.unlockRead(stamp);
	return result;
    }

    @Override
    public TickInfo getTickInfo() {
	return tick;
    }

    @Override
    public boolean isActive(final UUID action) {
	Objects.requireNonNull(action);
	synchronized (work) {
	    final Future<?> f = futures.get(action);
	    return f != null ? !f.isDone() : work.contains(new AbstractIdentifiable(action) {});
	}
    }

    /**
     * Pauses the engine ticking.
     */
    @Override
    public void pause() {
	if (running.getAndSet(false)) {
	    changeState(PAUSED);
	}
    }

    @Override
    public boolean removeEngineListener(final EngineListener listener) {
	return listeners.remove(listener);
    }

    @Override
    public <T> UUID scheduleAction(final Action<T> action, final T actor, final long initialDelay, final long period,
	    final TimeUnit unit) {
	if (initialDelay < 0 || period < 0) {
	    throw new IllegalArgumentException();
	}

	validateActive();

	final UUID key = UUID.randomUUID();

	synchronized (work) {
	    work.add(new Worker(key, Objects.requireNonNull(action), Objects.requireNonNull(actor), unit
		    .toNanos(initialDelay), unit.toNanos(period)));
	}

	return key;
    }

    @Override
    public <T> void setFirstAction(final Action<T> action, final T actor) {
	validateActive();
	first.set(action, actor);
    }

    @Override
    public <T> void setLastAction(final Action<T> action, final T actor) {
	validateActive();
	last.set(action, actor);
    }

    @Override
    public void stop() {
	if (running.getAndSet(false)) {
	    changeState(STOPPED);

	    synchronized (work) {
		work.clear();
	    }

	    executor.shutdown();

	    try {
		executor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.NANOSECONDS);
	    } catch (final InterruptedException e) {
		Thread.currentThread().interrupt();
	    }
	}
    }

    @Override
    public void tick() {
	if (!running.getAndSet(true)) {
	    changeState(IN_TICK);
	    executor.submit(control());
	}
    }

    private void validateActive() {
	if (getState() == STOPPED) {
	    throwRE(ENGINE_SHUTDOWN);
	}
    }
}
