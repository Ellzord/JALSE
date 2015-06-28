package jalse.actions;

import java.util.concurrent.TimeUnit;

/**
 * A mutable extension of {@link ActionContext}. This can be supplied at the appropriate level for
 * an editable context.<br>
 * <br>
 * Actions can be scheduled ({@link #schedule()}) and awaited ({@link #await()}) from this level.
 *
 * @author Elliot Ford
 * @param <T>
 *            Actor type (can be {@code ?} if no actor).
 *
 */
public interface SchedulableActionContext<T> extends ActionContext<T> {

    /**
     * Gets the action this context is for.
     *
     * @return Action.
     */
    Action<T> getAction();

    /**
     * Gets the initial delay.
     *
     * @param unit
     *            TimeUnit to convert to.
     * @return Initial delay in the supplied unit.
     */
    long getInitialDelay(TimeUnit unit);

    /**
     * Schedules the action for execution.
     */
    void schedule();

    /**
     * This is a convenience method for scheduling and then awaiting execution (or cancellation) of
     * the action.
     *
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    default void scheduleAndAwait() throws InterruptedException {
	schedule();

	if (!isDone()) {
	    await();
	}
    }

    /**
     * Sets the referenced actor.
     *
     * @param actor
     *            Actor.
     */
    void setActor(T actor);

    /**
     * Sets the initial delay.
     *
     * @param initialDelay
     *            Initial delay to set (can be {@code 0}).
     * @param unit
     *            TimeUnit delay is in.
     */
    void setInitialDelay(long initialDelay, TimeUnit unit);

    /**
     * Sets the period.
     *
     * @param period
     *            Period to set (can be {@code 0}).
     * @param unit
     *            TimeUnit period is in.
     */
    void setPeriod(long period, TimeUnit unit);

    /**
     * Sets whether the action continues to reschedule after an exception.
     *
     * @param periodicOnException
     *            Whether the action should continue.
     */
    void setPeriodicOnException(boolean periodicOnException);
}
