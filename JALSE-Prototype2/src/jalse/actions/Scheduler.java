package jalse.actions;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * This is the JALSE equivalent to {@link Executor}. {@link Action} can be
 * scheduled to be run once now, in the future or periodically at an interval.
 * When an action is run work is performed using a supplied actor, scheduler
 * defines the actor type as well as a means to maintain previously
 * scheduled/running actions.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type to schedule work against.
 */
public interface Scheduler<T> {

    /**
     * Cancels the action with the given ID.
     *
     * @param action
     *            ID of the action.
     * @return Whether the action was actually cancelled.
     * @throws NullPointerException
     *             If ID is null.
     */
    boolean cancel(final UUID action);

    /**
     * Whether the action is currently active in the engine.
     *
     * @param action
     *            Action ID.
     * @return Whether the action is still being executed or is about to.
     */
    boolean isActive(final UUID action);

    /**
     * Schedules an action to be performed next tick with zero delay.
     *
     * @param action
     *            Action to schedule.
     * @return Actions work ID.
     */
    default UUID schedule(final Action<T> action) {

	return schedule(action, 0L, TimeUnit.NANOSECONDS);
    }

    /**
     * Schedules an action to performed next tick with the specified delay. If
     * period is non-zero then this will be done at recurring intervals once per
     * tick.
     *
     * @param action
     *            Action to schedule.
     * @param initialDelay
     *            Delay before executing during tick.
     * @param period
     *            Recurring interval (should be 0 for run once actions).
     * @param unit
     *            Time unit of the delay and period.
     * @return Actions work ID.
     */
    UUID schedule(final Action<T> action, final long initialDelay, final long period, final TimeUnit unit);

    /**
     * Schedules an action to be performed next tick with the specified delay.
     *
     * @param action
     *            Action to schedule.
     * @param initialDelay
     *            Delay before executing during tick.
     * @param unit
     *            Time unit of the delay.
     * @return Actions work ID.
     */
    default UUID schedule(final Action<T> action, final long initialDelay, final TimeUnit unit) {

	return schedule(action, initialDelay, 0L, unit);
    }
}
