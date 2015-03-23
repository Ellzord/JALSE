package jalse.actions;

import static jalse.actions.Actions.requireNotStopped;

import java.util.concurrent.atomic.AtomicBoolean;

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
    protected class ManualContext<T> extends AbstractManualActionContext<T> {

	/**
	 * Creates a new ManualActionContext instance.
	 *
	 * @param action
	 *            Action this context is for.
	 */
	protected ManualContext(final Action<T> action) {
	    super(ManualActionEngine.this, action, bindings);
	}

	@Override
	protected void addAsWork() {
	    addWork(this);
	}

	@Override
	protected void removeAsWork() {
	    removeWork(this);
	}
    }

    private final ManualWorkQueue<ManualContext<?>> workQueue;
    private final MutableActionBindings bindings;
    private final AtomicBoolean ticking;
    private final AtomicBoolean stopped;

    /**
     * Creates a new instance of ManualActionEngine.
     */
    public ManualActionEngine() {
	workQueue = new ManualWorkQueue<>();
	bindings = new DefaultActionBindings();
	ticking = new AtomicBoolean();
	stopped = new AtomicBoolean();
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
    protected boolean addWork(final ManualContext<?> context) {
	requireNotStopped(this);

	return workQueue.addWaitingWork(context);
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return new ManualContext<>(action);
    }

    @Override
    public MutableActionBindings getBindings() {
	return bindings;
    }

    /**
     * Gets the engine's work queue.
     *
     * @return Manual work queue.
     */
    protected ManualWorkQueue<ManualContext<?>> getWorkQueue() {
	return workQueue;
    }

    @Override
    public boolean isPaused() {
	return !ticking.get();
    }

    @Override
    public boolean isStopped() {
	return stopped.get();
    }

    @Override
    public void pause() {}

    /**
     * Removes work from the engine.
     *
     * @param context
     *            Work to remove.
     * @return Whether the work was added before.
     */
    protected boolean removeWork(final ManualContext<?> context) {
	return workQueue.removeWaitingWork(context);
    }

    @Override
    public void resume() {
	requireNotStopped(this);
	if (!ticking.getAndSet(true)) {
	    return;
	}

	for (;;) {
	    final ManualContext<?> work = workQueue.pollReadyWork();
	    if (work == null) {
		break;
	    }

	    try {
		work.performAction();
	    } catch (final InterruptedException e) {
		ticking.set(false);
		Thread.currentThread().interrupt();
		throw new IllegalStateException(e);
	    }
	}

	ticking.set(false);
    }

    @Override
    public void stop() {
	requireNotStopped(this);

	ticking.set(false);
	workQueue.getWaitingWork().forEach(AbstractManualActionContext::cancel);
	stopped.set(true);
    }
}
