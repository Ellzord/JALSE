package jalse;

import jalse.actions.Action;
import jalse.misc.JALSEExceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
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
 * A task based engine for scheduling {@link Action} at different intervals.
 * This engine will use up to the number of threads specified upon creation
 * (depending on number of {@link Action} waiting to be executed per tick). The
 * engine will not be active upon creation it must be started with
 * {@link #tick()}.
 *
 * @author Elliot Ford
 *
 * @see Action#perform(Object, TickInfo)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class Engine {

    private class AtomicAction {

	private Action action;
	private Object actor;

	private AtomicAction() {

	    action = null;
	    actor = null;
	}

	public synchronized Action<?> getAction() {

	    return action;
	}

	public synchronized void perform() {

	    if (action != null && actor != null) {

		action.perform(actor, tick);
	    }
	}

	public synchronized void set(final Action action, final Object actor) {

	    this.action = action;
	    this.actor = actor;
	}
    }

    private class Worker implements Runnable, Comparable<Worker> {

	private final Action action;
	private final Object actor;
	private long estimated;
	private final UUID key;
	private final long period;

	private Worker(final UUID key, final Action action, final Object actor, final long initialDelay,
		final long period) {

	    this.key = key;
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
	public boolean equals(final Object obj) {

	    return key.equals(obj);
	}

	@Override
	public int hashCode() {

	    return key.hashCode();
	}

	@Override
	public void run() {

	    try {

		action.perform(actor, tick);
	    } catch (final Exception e) {

		if (e instanceof InterruptedException) {

		    Thread.currentThread().interrupt();
		}

		logger.log(Level.WARNING, "Error in worker", e);
	    } finally {

		phaser.arriveAndDeregister();
	    }
	}
    }

    private class WorkExecutor extends ThreadPoolExecutor {

	private final AtomicInteger active;

	private WorkExecutor(final int totalThreads) {

	    super(0, totalThreads, 0L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>() {

		private static final long serialVersionUID = 5154857493693362752L;

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

		    futures.remove(w.key);

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

    private static final Logger logger = Logger.getLogger(Engine.class.getName());

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    /**
     * The engine is not ticking and has been shutdown.
     */
    public static final int STOPPED = 0;

    /**
     * The engine is ready to be used.
     */
    public static final int INIT = 1;

    /**
     * The engine is currently in tick (processing).
     */
    public static final int IN_TICK = 2;

    /**
     * The engine is currently waiting.
     */
    public static final int IN_WAIT = 3;

    /**
     * The engine is paused but can be resumed.
     */
    public static final int PAUSED = 4;

    /**
     * When Java sleeps it can sometimes be inaccurate, the engine will sleep up
     * to this threshold then spin yield until the desired wake time.
     */
    public static final long SPIN_YIELD_THRESHOLD;

    /**
     * How long the engine will wait until it times out and interrupts running
     * threads on shutdown.
     */
    public static final long TERMINATION_TIMEOUT;

    static {

	final String syt = System.getProperty("jalse.engine.spin_yield_threshold");

	SPIN_YIELD_THRESHOLD = syt != null && syt.length() > 0 ? Long.valueOf(syt) : TimeUnit.MILLISECONDS.toNanos(10);

	final String tt = System.getProperty("jalse.engine.termination_timeout");

	TERMINATION_TIMEOUT = tt != null && tt.length() > 0 ? Long.valueOf(tt) : 2 * SECOND;
    }

    private static void parkNanos(final long end) {

	long timeLeft;

	while ((timeLeft = end - System.nanoTime()) > 0) {

	    if (timeLeft > SPIN_YIELD_THRESHOLD) {

		LockSupport.parkNanos(timeLeft - SPIN_YIELD_THRESHOLD);
	    }
	    else {

		Thread.yield();
	    }
	}
    }

    private static long requireNonNegative(final long value) {

	if (value <= 0) {

	    throw new IllegalArgumentException();
	}

	return value;
    }

    private final ThreadPoolExecutor executor;
    private final AtomicAction first;
    private final Map<UUID, Future<?>> futures;
    private final AtomicAction last;
    private final StampedLock lock;
    private final Phaser phaser;
    private final AtomicBoolean running;
    private final TickInfo tick;
    private final Queue<Worker> work;
    private int state;

    /**
     * Creates a new instance of Engine.
     *
     * @param tps
     *            Maximum ticks per second to run at.
     * @param totalThreads
     *            Maximum number of threads to use.
     */
    protected Engine(final int tps, final int totalThreads) {

	if (tps <= 0 || totalThreads <= 0) {

	    throw new IllegalArgumentException();
	}

	executor = new WorkExecutor(totalThreads < Integer.MAX_VALUE ? totalThreads + 1 : totalThreads);
	work = new PriorityQueue<>();
	phaser = new Phaser();
	futures = new HashMap<>();
	running = new AtomicBoolean();
	lock = new StampedLock();
	tick = new TickInfo(tps);
	first = new AtomicAction();
	last = new AtomicAction();

	state = INIT;
    }

    /**
     * Cancels the action with the given ID.
     *
     * @param action
     *            ID of the action.
     * @return Whether the action was cancelled.
     * @throws NullPointerException
     *             If ID is null.
     */
    public boolean cancel(final UUID action) {

	Objects.requireNonNull(action);

	boolean result = false;

	synchronized (work) {

	    final Future<?> f = futures.get(action);

	    if (f != null) {

		if (!f.isDone()) {

		    result = f.cancel(false);
		}
	    }
	    else {

		result = work.remove(action);
	    }
	}

	return result;
    }

    private void changeState(final int state) {

	final long stamp = lock.writeLock();

	boolean allow = false;
	RuntimeException e = null;

	switch (this.state) {

	case INIT:

	    allow = state != INIT;
	    break;

	case IN_TICK:
	case IN_WAIT:

	    allow = state == IN_TICK || state == IN_WAIT || state == PAUSED || state == STOPPED;
	    break;

	case PAUSED:

	    allow = state == IN_TICK || state == STOPPED;
	    break;

	case STOPPED:

	    e = JALSEExceptions.ENGINE_SHUTDOWN.get();
	    break;
	}

	if (allow) {

	    this.state = state;
	}

	lock.unlockWrite(stamp);

	if (e != null) {

	    throw e;
	}
    }

    private Runnable control() {

	/*
	 * Main work loop.
	 */
	return () -> {

	    long lastStart = System.nanoTime(), lastTpsCalc = System.nanoTime();
	    int currentTps = 0;

	    phaser.register();

	    while (running.get()) {

		changeState(IN_TICK);

		final long start = System.nanoTime(), interval = tick.getIntervalAsNanos(), estimatedEnd = start
			+ interval;

		tick.setDelta(start - lastStart);

		if (start - lastTpsCalc >= SECOND) {

		    tick.setCurrentTPS(currentTps);

		    lastTpsCalc = start;
		    currentTps = 0;
		}

		first.perform();

		synchronized (work) {

		    for (;;) {

			Worker w;

			w = work.peek();

			if (w == null || w.estimated >= estimatedEnd) {

			    break;
			}

			work.poll();

			phaser.register();

			futures.put(w.key, executor.submit(w));
		    }
		}

		phaser.arriveAndAwaitAdvance();

		last.perform();

		tick.incrementTicks();
		currentTps++;

		parkNanos(estimatedEnd);

		lastStart = start;
	    }

	    phaser.arriveAndDeregister();
	};
    }

    /**
     * Gets the first action to be run before other work is scheduled.
     *
     * @return First action to be run or null if not set.
     */
    protected Action<?> getFirstAction0() {

	return first.getAction();
    }

    /**
     * Gets the last action to be run after other work is scheduled.
     *
     * @return Last action to be run or null if not set.
     */
    protected Action<?> getLastAction0() {

	return last.getAction();
    }

    /**
     * Gets the current state of the engine.
     *
     * @return Current state.
     *
     */
    public int getState() {

	long stamp = lock.tryOptimisticRead();

	if (!lock.validate(stamp)) {

	    stamp = lock.readLock();
	}

	final int result = state;

	lock.unlockRead(stamp);

	return result;
    }

    /**
     * Gets the current TickInfo.
     *
     * @return Current tick information.
     */
    public TickInfo getTickInfo() {

	return tick;
    }

    /**
     * Whether the action is currently active in the engine.
     *
     * @param action
     *            Action ID.
     * @return Whether the action is still being executed or is about to.
     */
    public boolean isActive(final UUID action) {

	boolean result = false;

	synchronized (work) {

	    final Future<?> f = futures.get(action);

	    result = f != null ? !f.isDone() : work.contains(action);
	}

	return result;
    }

    /**
     * Pauses the engine ticking.
     */
    public void pause() {

	if (running.getAndSet(false)) {

	    changeState(PAUSED);
	}
    }

    /**
     * Schedules an action to be run on a specific actor. This can be run once
     * actions or recurring. An action may only be executed once per tick.
     *
     * @param action
     *            Action to perform.
     * @param actor
     *            Actor to perform on.
     * @param initialDelay
     *            Wait before running.
     * @param period
     *            Interval to repeat (should be 0 for run once actions).
     * @param unit
     *            TimeUnit for delay and period.
     * @return Scheduled action ID.
     *
     */
    protected UUID schedule0(final Action<?> action, final Object actor, final long initialDelay, final long period,
	    final TimeUnit unit) {

	final UUID key = UUID.randomUUID();

	synchronized (work) {

	    work.add(new Worker(key, Objects.requireNonNull(action), Objects.requireNonNull(actor), unit
		    .toNanos(initialDelay), unit.toNanos(requireNonNegative(period))));
	}

	return key;
    }

    /**
     * Sets the first action to be run before other work is scheduled.
     *
     * @param action
     *            Action to set.
     * @param actor
     *            Actor to perform action on.
     */
    protected void setFirstAction0(final Action<?> action, final Object actor) {

	first.set(action, actor);
    }

    /**
     * Sets the last action to be run after other work is scheduled.
     *
     * @param action
     *            Action to set.
     * @param actor
     *            Actor to perform action on.
     */
    protected void setLastAction0(final Action<?> action, final Object actor) {

	last.set(action, actor);
    }

    /**
     * Permanently stops the engine. All work that has not yet been executed
     * will be cancelled and all work currently executing will be given a
     * timeout before interruption.
     *
     * @return All work to be executed or currently executing.
     *
     * @see #TERMINATION_TIMEOUT
     */
    public List<Action<?>> stop() {

	final List<Action<?>> actions = new ArrayList<>();

	if (running.getAndSet(false)) {

	    changeState(STOPPED);

	    synchronized (work) {

		Worker w;

		while ((w = work.poll()) != null) {

		    actions.add(w.action);
		}
	    }

	    executor.shutdown();

	    try {

		if (executor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.NANOSECONDS)) {

		    for (final Runnable r : executor.shutdownNow()) {

			actions.add(((Worker) r).action);
		    }
		}
	    } catch (final InterruptedException e) {

		Thread.currentThread().interrupt();
	    }
	}

	return actions;
    }

    /**
     * Starts ticking the engine.
     */
    public void tick() {

	if (!running.getAndSet(true)) {

	    changeState(IN_TICK);

	    executor.submit(control());
	}
    }
}
