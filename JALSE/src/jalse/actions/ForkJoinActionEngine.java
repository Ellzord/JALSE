package jalse.actions;

import static jalse.actions.Actions.requireNotShutdown;
import static jalse.actions.Actions.requireNotStopped;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.atomic.AtomicInteger;

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
    protected class ForkJoinContext<T> extends AbstractManualActionContext<T> {

	/**
	 * Creates a new ForkJoinContext.
	 *
	 * @param action
	 *            Action this context is for.
	 */
	protected ForkJoinContext(final Action<T> action) {
	    super(ForkJoinActionEngine.this, action, getBindings());
	}

	@Override
	protected void addAsWork() {
	    addWork(this);
	}

	@Override
	public void removeAsWork() {
	    removeWork(this);
	}
    }

    private class ForkJoinContextWorker implements Runnable {

	private final ManagedBlocker blocker;

	private ForkJoinContextWorker() {
	    blocker = new ManagedBlocker() {

		@Override
		public boolean block() throws InterruptedException {
		    workQueue.awaitNextReadyWork();
		    return true;
		}

		@Override
		public boolean isReleasable() {
		    return workQueue.isWorkReady() || !workQueue.isWorkWaiting();
		}
	    };
	}

	@Override
	public void run() {
	    while (workQueue.isWorkWaiting()) {
		try {
		    awaitResumed(); // Paused
		} catch (final InterruptedException e) {
		    break;
		}

		try {
		    ForkJoinPool.managedBlock(blocker); // Allows other ForkJoinThreads to run
		} catch (final InterruptedException e) {}

		if (!workQueue.isWorkWaiting()) {
		    break;
		}

		final ForkJoinContext<?> work = workQueue.pollReadyWork();
		if (work != null) {
		    freeWorkers.decrementAndGet(); // Will be busy working

		    try {
			work.performAction();
		    } catch (final InterruptedException e) {
			break; // Cancellation
		    } finally {
			if (!freeWorkers.compareAndSet(0, 1)) { // Ready again
			    return;
			}
		    }
		}
	    }
	    freeWorkers.decrementAndGet(); // Finished.
	}
    }

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

    private final ManualWorkQueue<ForkJoinContext<?>> workQueue;
    private final AtomicInteger freeWorkers;

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
	workQueue = new ManualWorkQueue<>();
	freeWorkers = new AtomicInteger();
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

    /**
     * Adds work to the engine.
     *
     * @param context
     *            Work to add.
     *
     * @return Whether the work was not already in the queue.
     *
     * @see Actions#requireNotStopped(ActionEngine)
     */
    protected boolean addWork(final ForkJoinContext<?> context) {
	requireNotStopped(this);

	final boolean result;
	if (result = workQueue.addWaitingWork(context)) {
	    addWorkerIfNeeded();
	}

	return result;
    }

    /**
     * Starts a worker if needed (waiting work and no free workers).
     */
    protected void addWorkerIfNeeded() {
	if (workQueue.isWorkWaiting() && freeWorkers.compareAndSet(0, 1)) { // Only needs one worker
	    executorService.submit(new ForkJoinContextWorker());
	}
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return new ForkJoinContext<>(action);
    }

    /**
     * Gets the engine's work queue.
     *
     * @return Manual work queue.
     */
    protected ManualWorkQueue<ForkJoinContext<?>> getWorkQueue() {
	return workQueue;
    }

    /**
     * Removes work from the engine.
     *
     * @param context
     *            Work to remove.
     * @return Whether the work was added before.
     */
    protected boolean removeWork(final ForkJoinContext<?> context) {
	return workQueue.removeWaitingWork(context);
    }

    @Override
    public void stop() {
	requireNotShutdown(executorService);
	super.stop(); // Shutdown pool first
	workQueue.getWaitingWork().forEach(ForkJoinContext::cancel);
    }
}
