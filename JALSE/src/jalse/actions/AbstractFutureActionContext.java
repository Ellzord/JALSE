package jalse.actions;

import static jalse.actions.Actions.emptyActionBindings;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;

/**
 * An abstract implementation of {@link MutableActionContext} that is designed to be used with
 * {@link ExecutorService}. This is a convenience class for creating an {@link ActionEngine}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public abstract class AbstractFutureActionContext<T> extends BaseActionContext<T> {

    private final StampedLock lock;
    private Future<?> future;

    /**
     * Creates a new instance of AbstractFutureActionContext with the supplied engine and action.
     *
     * @param engine
     *            Parent engine.
     * @param action
     *            Action this context is for.
     */
    protected AbstractFutureActionContext(final ActionEngine engine, final Action<T> action) {
	this(engine, action, emptyActionBindings());
    }

    /**
     * Creates a new instance of AbstractFutureActionContext with the supplied engine, action and
     * source bindings.
     *
     * @param engine
     *            Parent engine.
     * @param sourceBindings
     *            Bindings to shallow copy.
     *
     * @param sourceBindings
     */
    protected AbstractFutureActionContext(final ActionEngine engine, final Action<T> action,
	    final ActionBindings sourceBindings) {
	super(engine, action, sourceBindings);
	lock = new StampedLock();
	future = null;
    }

    @Override
    public void await() throws InterruptedException {
	if (isPeriodic()) {
	    throw new UnsupportedOperationException("Cannot await periodic actions");
	}

	final Future<?> f = getFuture();
	if (f == null || f.isDone()) {
	    return;
	}

	try {
	    f.get();
	} catch (final ExecutionException e) {}
    }

    @Override
    public boolean cancel() {
	final Future<?> f = getFuture();
	return f != null && f.cancel(true);
    }

    /**
     * Gets the future associated to the action task.
     *
     * @return Future or null if none has been set.
     */
    protected Future<?> getFuture() {
	long stamp = lock.tryOptimisticRead();
	Future<?> future = this.future;
	if (!lock.validate(stamp)) {
	    stamp = lock.readLock();
	    try {
		future = this.future;
	    } finally {
		lock.unlockRead(stamp);
	    }
	}
	return future;
    }

    @Override
    public boolean isCancelled() {
	final Future<?> f = getFuture();
	return f != null && f.isCancelled();
    }

    @Override
    public boolean isDone() {
	final Future<?> f = getFuture();
	return f != null && f.isDone();
    }

    /**
     * Sets the future of the associated action task.
     *
     * @param future
     *            Future to set.
     */
    protected void setFuture(final Future<?> future) {
	final long stamp = lock.writeLock();
	try {
	    this.future = future;
	} finally {
	    lock.unlockWrite(stamp);
	}
    }
}
