package jalse.actions;

import static jalse.actions.Actions.requireNotStopped;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manual-tick implementation of {@link ActionEngine}. ManualActionEngine uses no additional
 * threads and will not run any actions until {@link #resume()} is called. When the engine is
 * ticking all jobs that should be executed will be (even if past their estimated schedule time).
 *
 * @author Elliot Ford
 *
 */
public class ManualActionEngine implements ActionEngine {

    /**
     * Manual action context.
     *
     * @author Elliot Ford
     *
     * @param <T>
     *            Actor type.
     */
    protected class ManualContext<T> extends AbstractActionContext<T> implements Runnable {

	private final Lock lock;
	private final Condition ran;
	private volatile boolean cancelled;
	private volatile boolean done;
	private volatile long estimated;

	/**
	 * Creates a new ManualActionContext instance.
	 *
	 * @param action
	 *            Action this context is for.
	 */
	protected ManualContext(final Action<T> action) {
	    super(ManualActionEngine.this, action, bindings);
	    lock = new ReentrantLock();
	    ran = lock.newCondition();
	    reset();
	}

	@Override
	public void await() throws InterruptedException {
	    lock.lockInterruptibly();
	    try {
		while (!done) {
		    ran.await();
		}
	    } finally {
		lock.unlock();
	    }
	}

	@Override
	public boolean cancel() {
	    if (cancelled) {
		return false;
	    }

	    cancelled = true;
	    final boolean result = removeWork(this);
	    signalRan();

	    return result;
	}

	/**
	 * Gets the ideal estimated execution time (nanos).
	 *
	 * @return gets the estimated time of execution.
	 *
	 * @see System#nanoTime()
	 */
	protected long getEstimated() {
	    return estimated;
	}

	@Override
	public boolean isCancelled() {
	    return cancelled;
	}

	@Override
	public boolean isDone() {
	    return done;
	}

	private void reset() {
	    done = false;
	    cancelled = false;
	    estimated = 0L;
	}

	@Override
	public void run() {
	    try {
		getAction().perform(this);
	    } catch (final Exception e) {
		if (e instanceof InterruptedException) {
		    Thread.currentThread().interrupt();
		    cancelled = true;
		}

		logger.log(Level.WARNING, "Error performing action", e);
	    }

	    signalRan();

	    if (isPeriodic() && !isCancelled()) {
		schedule(getPeriod(TimeUnit.NANOSECONDS));
	    }
	}

	@Override
	public void schedule() {
	    if (!done) {
		schedule(getInitialDelay(TimeUnit.NANOSECONDS));
	    }
	}

	/**
	 * Schedules the action for execution (nanos).
	 *
	 * @param delay
	 *            Delay before schedule.
	 */
	protected void schedule(final long delay) {
	    if (done) {
		reset();
	    }

	    estimated = System.nanoTime() + delay;
	    addWork(this);
	}

	@Override
	public void scheduleAndAwait() throws InterruptedException {
	    schedule();
	    await();
	}

	private void signalRan() {
	    done = true;
	    lock.lock();
	    try {
		ran.signalAll();
	    } finally {
		lock.unlock();
	    }
	}
    }

    private static final Logger logger = Logger.getLogger(ManualActionEngine.class.getName());

    private final Queue<ManualContext<?>> workQueue;
    private final MutableActionBindings bindings;
    private volatile boolean ticking;
    private volatile boolean stopped;

    /**
     * Creates a new instance of ManualActionEngine.
     */
    public ManualActionEngine() {
	workQueue = new PriorityQueue<>();
	bindings = new DefaultActionBindings();
	ticking = false;
	stopped = false;
    }

    /**
     * Adds work to the engine.
     *
     * @param context
     *            Work to add.
     *
     * @see Actions#requireNotStopped(ActionEngine)
     */
    protected void addWork(final ManualContext<?> context) {
	requireNotStopped(this);

	synchronized (workQueue) {
	    if (!workQueue.contains(context)) {
		workQueue.add(context);
	    }
	}
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return new ManualContext<>(action);
    }

    @Override
    public MutableActionBindings getBindings() {
	return bindings;
    }

    @Override
    public boolean isPaused() {
	return !ticking;
    }

    @Override
    public boolean isStopped() {
	return stopped;
    }

    @Override
    public void pause() {}

    /**
     * Removes work from the engine.
     *
     * @param context
     *            Work to remove.
     * @return Whether the work was waiting to be executed.
     */
    protected boolean removeWork(final ManualContext<?> context) {
	synchronized (workQueue) {
	    return workQueue.remove(context);
	}
    }

    @Override
    public void resume() {
	requireNotStopped(this);
	if (ticking) {
	    return;
	}

	final List<ManualContext<?>> worked = new ArrayList<>();

	synchronized (workQueue) {
	    ticking = true;

	    final long now = System.nanoTime();
	    for (;;) {
		final ManualContext<?> work = workQueue.peek();
		if (work == null || now >= work.getEstimated()) {
		    break;
		}
		workQueue.remove();
		worked.add(work);
	    }
	}

	worked.forEach(ManualContext::run);

	synchronized (workQueue) {
	    ticking = false;
	}
    }

    @Override
    public void stop() {
	requireNotStopped(this);

	synchronized (workQueue) {
	    ticking = false;

	    for (;;) {
		final ManualContext<?> work = workQueue.poll();
		if (work == null) {
		    break;
		}
		work.cancel();
	    }

	    stopped = false;
	}
    }
}
