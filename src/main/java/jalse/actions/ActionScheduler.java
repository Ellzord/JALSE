package jalse.actions;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * This is the JALSE equivalent to {@link Executor}. {@link Action} can be scheduled to be run once
 * now, in the future and periodically at an interval. When an action is run work can be performed
 * using a supplied actor, scheduler defines the actor type as well as a means to maintain
 * previously scheduled/running actions.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type to schedule referencing work.
 */
public interface ActionScheduler<T> {

    /**
     * Cancels all tasks currently running/scheduled (scheduled by this).
     */
    void cancelAllScheduledForActor();

    /**
     * Creates a new mutable context for the supplied action and this actor.
     *
     * @param action
     *            Action to create context for.
     * @return Context bound to the action and actor.
     */
    MutableActionContext<T> newContextForActor(Action<T> action);

    /**
     * Schedules an action for immediate execution.
     *
     * @param action
     *            Action to schedule.
     * @return Context associated with the action (immutable).
     */
    default ActionContext<T> scheduleForActor(final Action<T> action) {
	return scheduleForActor(action, 0L, TimeUnit.NANOSECONDS);
    }

    /**
     * Schedules an action for execution with a supplied initial delay and repeat period.
     *
     * @param action
     *            Action to schedule.
     * @param initialDelay
     *            Initial delay before schedule (can be {@code 0}).
     * @param period
     *            Period for repeating (can be {@code 0}).
     * @param unit
     *            Time unit of initial delay and period.
     * @return Context associated with the action (immutable).
     */
    ActionContext<T> scheduleForActor(final Action<T> action, final long initialDelay, final long period,
	    final TimeUnit unit);

    /**
     * Schedules an action to be executed after the supplied delay.
     *
     * @param action
     *            Action to schedule.
     * @param initialDelay
     *            Delay before schedule.
     * @param unit
     *            TimeUnit of the delay.
     * @return Context associated with the action (immutable).
     */
    default ActionContext<T> scheduleForActor(final Action<T> action, final long initialDelay, final TimeUnit unit) {
	return scheduleForActor(action, initialDelay, 0L, unit);
    }
}
