package jalse.engine.actions;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple {@link Runnable} implementation for executing {@link Action}. This provides all the
 * information needed to perform an action and reschedule for future execution. Estimation is done
 * using {@link System#nanoTime()}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public class ActionJob<T> implements Runnable, Comparable<ActionJob<?>> {

    private static final Logger logger = Logger.getLogger(ActionJob.class.getName());

    private final Action<T> action;
    private final ActionContext<T> context;
    private long estimated;

    /**
     * Creates a new ActionJob with the supplied information.
     *
     * @param action
     *            Action to run.
     * @param context
     *            Context to supply.
     * @param initialDelay
     *            Initial delay before running action (for estimation).
     */
    public ActionJob(final Action<T> action, final ActionContext<T> context, final long initialDelay) {
	this.action = Objects.requireNonNull(action);
	this.context = Objects.requireNonNull(context);
	estimated = System.nanoTime() + initialDelay;
    }

    @Override
    public int compareTo(final ActionJob<?> o) {
	return estimated < o.estimated ? -1 : estimated == o.estimated ? 0 : 1;
    }

    /**
     * Re-calculates estimated execution for repeating.
     */
    public void estimateForReschedule() {
	if (!context.isPeriodic()) {
	    throw new UnsupportedOperationException();
	}
	estimated = System.nanoTime() + context.getPeriod();
    }

    /**
     * Gets the action that will be run.
     *
     * @return Action.
     */
    public Action<T> getAction() {
	return action;
    }

    /**
     * Gets the context to supply to the action.
     *
     * @return Context.
     */
    public ActionContext<T> getContext() {
	return context;
    }

    /**
     * Gets the estimated execution time.
     *
     * @return Estimated time.
     */
    public long getEstimated() {
	return estimated;
    }

    @Override
    public void run() {
	try {
	    action.perform(context);
	} catch (final Exception e) {
	    if (e instanceof InterruptedException) {
		Thread.currentThread().interrupt();
	    }

	    logger.log(Level.WARNING, "Error performing action", e);
	}
    }
}
