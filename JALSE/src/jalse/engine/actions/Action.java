package jalse.engine.actions;

/**
 * Action is the JALSE equivalent of {@link Runnable}. Actions are performed using a given actor and
 * can be scheduled to be run once now, in the future or periodically at an interval.
 * {@link ActionContext} will be supplied on every execution of an action, this will be current and
 * contain the delta between the last tick. Actions are generally scheduled by
 * {@link ActionScheduler} for the actor type suitable for the desired result.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied.
 *
 * @see ActionScheduler#scheduleAction(Action, long, long, java.util.concurrent.TimeUnit)
 * @see ActionContext#getTickDeltaAsNanos()
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Performs the actions using the supplied action context.
     *
     * @param actor
     *            Actor to use.
     * @param context
     *            Current action context.
     *
     * @see Runnable#run()
     */
    void perform(ActionContext<T> context);
}
