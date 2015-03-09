package jalse.actions;

/**
 * Action is the JALSE equivalent of {@link Runnable}. Actions are performed using a given actor and
 * can be scheduled to be run once now, in the future or periodically at an interval.
 * {@link TickInfo} will be supplied on every execution of an action, this will be current and
 * contain the delta between the last tick. Actions are generally scheduled by {@link Scheduler} for
 * the actor type suitable for the desired result.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied.
 *
 * @see Scheduler#scheduleAction(Action, long, long, java.util.concurrent.TimeUnit)
 * @see TickInfo#getDelta()
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Performs the actions using the supplied actor and given tick information.
     *
     * @param actor
     *            Actor to use.
     * @param tick
     *            Current tick information
     *
     * @see Runnable#run()
     */
    void perform(T actor, TickInfo tick);

}
