package jalse.engine.actions;

import jalse.engine.Engine;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A task based engine definition for scheduling {@link Action}. Work can be scheduled immediately,
 * sometime in the future or periodically. This work can be tracked and cancelled. Specific actions
 * can be set to run first and last within a tick.
 *
 * @author Elliot Ford
 *
 * @see Action#perform(ActionContext)
 *
 * @see ContinuousActionEngine
 * @see ManualActionEngine
 *
 */
public interface ActionEngine extends Engine {

    /**
     * Cancels the action with the given ID.
     *
     * @param action
     *            ID of the action.
     * @return Whether the action was cancelled.
     * @throws NullPointerException
     *             If ID is null.
     */
    boolean cancel(UUID action);

    /**
     * Whether the action is currently active in the engine.
     *
     * @param action
     *            Action ID.
     * @return Whether the action is still being executed or is about to.
     */
    boolean isActive(UUID action);

    /**
     * Schedules an action to be run on a specific actor. This can be run once actions or recurring.
     * An action may only be executed once per tick.
     *
     * @param action
     *            Action to perform.
     * @param actor
     *            Actor to perform on.
     * @param initialDelay
     *            Wait before running.
     * @param period
     *            Interval to repeat (should be 0 for run once actions).
     * @param unit
     *            TimeUnit for delay and period.
     * @return Scheduled action ID.
     *
     */
    <T> UUID scheduleAction(final Action<T> action, final T actor, final long initialDelay, final long period,
	    final TimeUnit unit);

    /**
     * Sets the first action to be run before other work is scheduled.
     *
     * @param action
     *            Action to set.
     * @param actor
     *            Actor to perform action on.
     */
    <T> void setFirstAction(final Action<T> action, final T actor);

    /**
     * Sets the last action to be run after other work is scheduled.
     *
     * @param action
     *            Action to set.
     * @param actor
     *            Actor to perform action on.
     */
    <T> void setLastAction(final Action<T> action, final T actor);
}
