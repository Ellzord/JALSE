package jalse.actions;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link ActionEngine} based on {@link ForkJoinPool}. Scheduling is achieved
 * using {@link ForkJoinPool#managedBlock(ManagedBlocker)} so many threads may be used but not all
 * executing {@link Action}.<br>
 * <br>
 * ForkJoinActionEngine offers an instance backed on {@link ForkJoinPool#commonPool()} that cannot
 * be stopped or paused ({@link #commonPoolEngine()}).
 *
 * @author Elliot Ford
 *
 */
public class ForkJoinActionEngine extends AbstractActionEngine {

    /**
     * Fork join context.
     *
     * @author Elliot Ford
     *
     * @param <T>
     *            Actor type.
     */
    protected class ForkJoinContext<T> extends AbstractFutureActionContext<T> implements Runnable {

	private final Lock lock;
	private final Condition cancelled;
	private final ManagedBlocker blocker;
	private volatile long blockUntil;

	/**
	 * Creates a new ForkJoinContext.
	 *
	 * @param action
	 *            Action this context is for.
	 */
	protected ForkJoinContext(final Action<T> action) {
	    super(ForkJoinActionEngine.this, action, getBindings());
	    lock = new ReentrantLock();
	    cancelled = lock.newCondition();
	    blocker = new ManagedBlocker() {

		@Override
		public boolean block() throws InterruptedException {
		    awaitDelay();
		    return true;
		}

		@Override
		public boolean isReleasable() {
		    return delayReached() || isCancelled();
		}
	    };
	}

	private void awaitDelay() throws InterruptedException {
	    lock.lockInterruptibly();
	    try {
		long wait = blockUntil - System.nanoTime();
		while (wait > 0 && !isCancelled()) {
		    wait = cancelled.awaitNanos(wait);
		}
	    } finally {
		lock.unlock();
	    }
	}

	@Override
	public boolean cancel() {
	    signalCancelled();
	    return super.cancel();
	}

	private boolean delayReached() {
	    return System.nanoTime() >= blockUntil;
	}

	@Override
	public void run() {
	    /*
	     * ForkJoinPool doesn't use interrupted state to manage threads.
	     */
	    try {
		awaitResumed();
	    } catch (final InterruptedException e) {
		return;
	    }

	    try {
		ForkJoinPool.managedBlock(blocker);
	    } catch (final InterruptedException e) {}

	    if (isCancelled()) {
		return;
	    }

	    try {
		getAction().perform(this);
	    } catch (final Exception e) {
		logger.log(Level.WARNING, "Error performing action", e);
	    }

	    if (isPeriodic() && !isCancelled()) {
		schedule(getPeriod(TimeUnit.NANOSECONDS));
	    }
	}

	@Override
	public void schedule() {
	    if (!isDone()) {
		schedule(getInitialDelay(TimeUnit.NANOSECONDS));
	    }
	}

	/**
	 * Schedules this action for execution after a specified delay (nanos).
	 *
	 * @param delay
	 *            Delay before scheduling.
	 */
	protected void schedule(final long delay) {
	    blockUntil = System.nanoTime() + delay;
	    setFuture(executorService.submit(this));
	}

	private void signalCancelled() {
	    lock.lock();
	    try {
		cancelled.signal();
	    } finally {
		lock.unlock();
	    }
	}
    }

    private static final Logger logger = Logger.getLogger(ForkJoinActionEngine.class.getName());

    private static final ForkJoinActionEngine commonPoolEngine = new ForkJoinActionEngine(ForkJoinPool.commonPool()) {

	@Override
	public void pause() {};

	@Override
	public void stop() {};
    };

    /**
     * Gets the common ForkJoinActionEngine instance backed by {@link ForkJoinPool#commonPool()}
     * (this cannot be paused or stopped).
     *
     * @return Common engine instance.
     */
    public static ForkJoinActionEngine commonPoolEngine() {
	return commonPoolEngine;
    }

    /**
     * Creates a new ForkJoinActionEngine instance with the default parallelism.
     *
     * @see Runtime#availableProcessors()
     */
    public ForkJoinActionEngine() {
	this(Runtime.getRuntime().availableProcessors());
    }

    private ForkJoinActionEngine(final ForkJoinPool pool) {
	super(pool);
    }

    /**
     * Creates a new ForkJoinActionEngine with the supplied parallelism.
     *
     * @param parallelism
     *            The parallelism level.
     */
    public ForkJoinActionEngine(final int parallelism) {
	this(new ForkJoinPool(parallelism));
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return new ForkJoinContext<>(action);
    }
}
