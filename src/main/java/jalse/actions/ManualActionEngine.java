package jalse.actions;

import static jalse.actions.Actions.requireNotStopped;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public class ManualContext<T> extends AbstractManualActionContext<T> {

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

    private static final Logger logger = Logger.getLogger(ManualActionEngine.class.getName());

    private final ManualWorkQueue<ManualContext<?>> workQueue;
    private final ActionBindings bindings;
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
    public ActionBindings getBindings() {
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
    public <T> ManualContext<T> newContext(final Action<T> action) {
	return new ManualContext<>(action);
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

	final List<ManualContext<?>> batch = new ArrayList<>();

	// Create batch of work
	for (;;) {
	    final ManualContext<?> work = workQueue.pollReadyWork();
	    if (work == null) { // No more ready work
		break;
	    }
	    batch.add(work);
	}

	// Perform batch
	try {
	    batch.forEach(work -> {
		try {
		    work.performAction();
		} catch (final InterruptedException e) {
		    Thread.currentThread().interrupt();
		} catch (final Exception e) {
		    logger.log(Level.WARNING, "Error performing action", e);
		}
	    });
	} finally {
	    ticking.set(false);
	}
    }

    @Override
    public void stop() {
	requireNotStopped(this);

	ticking.set(false);
	workQueue.getWaitingWork().forEach(AbstractManualActionContext::cancel);
	stopped.set(true);
    }
}
