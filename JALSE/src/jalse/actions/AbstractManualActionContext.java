package jalse.actions;

import static jalse.actions.Actions.emptyActionBindings;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A abstract implementation of {@link MutableActionContext} that is designed to be used manually.
 * This class should be used whenever controlling the execution state of work is important. The
 * action should be performed with {@link #performAction()}. This is a convenience class for
 * creating an {@link ActionEngine}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 *
 * @see ManualWorkQueue
 * @see ManualActionEngine
 * @see ForkJoinActionEngine
 */
public abstract class AbstractManualActionContext<T> extends AbstractActionContext<T> implements
	Comparable<AbstractManualActionContext<?>> {

    private static final Logger logger = Logger.getLogger(AbstractManualActionContext.class.getName());

    private final Lock lock;
    private final Condition ran;
    private final AtomicBoolean cancelled;
    private final AtomicBoolean done;
    private final AtomicBoolean performing;
    private final AtomicLong estimated;

    /**
     * Creates a new instance of AbstractManualActionContext with the supplied engine and action.
     *
     * @param engine
     *            Parent engine.
     * @param action
     *            Action this context is for.
     */
    protected AbstractManualActionContext(final ActionEngine engine, final Action<T> action) {
	this(engine, action, emptyActionBindings());
    }

    /**
     * Creates a new instance of AbstractManualActionContext with the supplied engine, action and
     * source bindings.
     *
     * @param engine
     *            Parent engine.
     * @param sourceBindings
     *            Bindings to shallow copy.
     *
     * @param sourceBindings
     */
    protected AbstractManualActionContext(final ActionEngine engine, final Action<T> action,
	    final ActionBindings sourceBindings) {
	super(engine, action, sourceBindings);
	lock = new ReentrantLock();
	ran = lock.newCondition();
	done = new AtomicBoolean();
	performing = new AtomicBoolean();
	cancelled = new AtomicBoolean();
	estimated = new AtomicLong();
    }

    /**
     * Used to add this context as work to a queue.
     */
    protected abstract void addAsWork();

    @Override
    public void await() throws InterruptedException {
	if (isPeriodic()) {
	    throw new UnsupportedOperationException("Cannot await periodic actions");
	}

	lock.lockInterruptibly();
	try {
	    while (!isDone()) {
		ran.await();
	    }
	} finally {
	    lock.unlock();
	}
    }

    @Override
    public boolean cancel() {
	if (isDone()) {
	    return false;
	}

	cancelled.set(true);
	done.set(true);

	removeAsWork();

	if (!performing.get()) {
	    signalRan();
	}

	return true;
    }

    @Override
    public int compareTo(final AbstractManualActionContext<?> o) {
	final long estimated = getEstimated();
	final long otherEstimated = o.getEstimated();
	return estimated < otherEstimated ? -1 : estimated == otherEstimated ? 0 : 1;
    }

    /**
     * Gets the ideal estimated execution time (nanos).
     *
     * @return gets the estimated time of execution.
     *
     * @see System#nanoTime()
     */
    public long getEstimated() {
	return estimated.get();
    }

    @Override
    public boolean isCancelled() {
	return cancelled.get();
    }

    @Override
    public boolean isDone() {
	return done.get();
    }

    /**
     * Whether the action is currently being performed.
     *
     * @return Whether performing action.
     */
    public boolean isPeforming() {
	return performing.get();
    }

    /**
     * Performs the action (setting context state).
     *
     * @throws InterruptedException
     *             If action throws this or this is interrupted.
     */
    public void performAction() throws InterruptedException {
	if (isDone()) {
	    return;
	}

	performing.set(true);

	try {
	    getAction().perform(this);
	} catch (final InterruptedException e) {
	    cancelled.set(true);
	    throw e;
	} catch (final Exception e) {
	    logger.log(Level.WARNING, "Error performing action", e);
	} finally {
	    performing.set(false);
	    done.set(true);
	    signalRan();
	}

	if (isCancelled()) {
	    return;
	}

	if (isPeriodic()) {
	    estimated.set(System.nanoTime() + getPeriod(TimeUnit.NANOSECONDS));
	    done.set(false);
	    addAsWork();
	}
    }

    /**
     * Used to remove context as work from a queue.
     */
    protected abstract void removeAsWork();

    /**
     * Resets the context to its starting state.
     */
    protected void reset() {
	done.set(false);
	performing.set(false);
	cancelled.set(false);
	estimated.set(0L);
    }

    @Override
    public void schedule() {
	if (!isDone()) {
	    estimated.set(System.nanoTime() + getInitialDelay(TimeUnit.NANOSECONDS));
	    addAsWork();
	}
    }

    @Override
    public void scheduleAndAwait() throws InterruptedException {
	schedule();
	await();
    }

    private void signalRan() {
	lock.lock();
	try {
	    ran.signalAll();
	} finally {
	    lock.unlock();
	}
    }
}
