package org.jalse;

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

import org.jalse.actions.Action;
import org.jalse.misc.JALSEException;

@SuppressWarnings({ "rawtypes", "unchecked" })
abstract class Engine {

    private class AtomicAction {

	private Action action;
	private Object actor;

	private AtomicAction() {

	    action = null;
	    actor = null;
	}

	private Action<?> getAction() {

	    Action<?> result;

	    synchronized (AtomicAction.this) {

		result = action;
	    }

	    return result;
	}

	private void perform() {

	    synchronized (AtomicAction.this) {

		if (action != null && actor != null) {

		    action.perform(actor, tick);
		}
	    }
	}

	private void set(final Action action, final Object actor) {

	    synchronized (AtomicAction.this) {

		this.action = action;
		this.actor = actor;
	    }
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

    private static final Logger logger = Logger.getLogger(Engine.class.getCanonicalName());

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    private static final long SPIN_YIELD_THRESHOLD;

    private static final long TERMINATION_TIMEOUT;

    static {

	/**
	 * Spin yield threshold - When Java sleeps it can sometimes be
	 * inaccurate, the engine will sleep up to this threshold then spin
	 * yield until the desired time.
	 */
	final String syt = System.getProperty("jalse.spin_yield_threshold");

	SPIN_YIELD_THRESHOLD = syt != null && syt.length() > 0 ? Long.valueOf(syt) : TimeUnit.MILLISECONDS.toNanos(10);

	/**
	 * Termination timeout - How long the engine will wait until it times
	 * out and interrupts running threads on shutdown.
	 */
	final String tt = System.getProperty("jalse.termination_timeout");

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
    private State state;

    private final TickInfo tick;

    private final Queue<Worker> work;

    Engine(final int tps, final int totalThreads) {

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
	state = State.INIT;
	first = new AtomicAction();
	last = new AtomicAction();
    }

    public boolean cancel(final UUID action) {

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

    private void changeState(final State state) {

	final long stamp = lock.writeLock();

	boolean allow = false;
	JALSEException je = null;

	switch (this.state) {

	case INIT:

	    allow = state != State.INIT;
	    break;

	case IN_TICK:
	case IN_WAIT:

	    allow = state == State.IN_TICK || state == State.IN_WAIT || state == State.PAUSED || state == State.STOPPED;
	    break;

	case PAUSED:

	    allow = state == State.IN_TICK || state == State.STOPPED;
	    break;

	case STOPPED:

	    je = JALSEException.ENGINE_SHUTDOWN.get();
	    break;
	}

	if (allow) {

	    this.state = state;
	}

	lock.unlockWrite(stamp);

	if (je != null) {

	    throw je;
	}
    }

    private Runnable control() {

	return () -> {

	    long lastStart = System.nanoTime(), lastTpsCalc = System.nanoTime();
	    int currentTps = 0;

	    phaser.register();

	    while (running.get()) {

		changeState(State.IN_TICK);

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

    Action<?> getFirstAction0() {

	return first.getAction();
    }

    Action<?> getLastAction0() {

	return last.getAction();
    }

    public State getState() {

	long stamp = lock.tryOptimisticRead();

	if (!lock.validate(stamp)) {

	    stamp = lock.readLock();
	}

	final State result = state;

	lock.unlockRead(stamp);

	return result;
    }

    public TickInfo getTickInfo() {

	return tick;
    }

    public boolean isActive(final UUID action) {

	boolean result = false;

	synchronized (work) {

	    final Future<?> f = futures.get(action);

	    result = f != null ? !f.isDone() : work.contains(action);
	}

	return result;
    }

    public void pause() {

	if (running.getAndSet(false)) {

	    changeState(State.PAUSED);
	}
    }

    UUID schedule0(final Action<?> action, final Object actor, final long initialDelay, final long period,
	    final TimeUnit unit) {

	final UUID key = UUID.randomUUID();

	synchronized (work) {

	    work.add(new Worker(key, Objects.requireNonNull(action), Objects.requireNonNull(actor), unit
		    .toNanos(initialDelay), unit.toNanos(requireNonNegative(period))));
	}

	return key;
    }

    void setFirstAction0(final Action<?> action, final Object actor) {

	first.set(action, actor);
    }

    void setLastAction0(final Action<?> action, final Object actor) {

	last.set(action, actor);
    }

    public List<Action<?>> stop() {

	final List<Action<?>> actions = new ArrayList<>();

	if (running.getAndSet(false)) {

	    changeState(State.STOPPED);

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

    public void tick() {

	if (!running.getAndSet(true)) {

	    changeState(State.IN_TICK);

	    executor.submit(control());
	}
    }
}
