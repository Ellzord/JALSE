package jalse.actions;

/**
 * Action is the JALSE equivalent of {@link Runnable}. They must be scheduled by
 * {@link ActionEngine} but can also be delegate scheduled referencing a specific actor with
 * {@link ActionScheduler}. Actions are the only non-optional value when creating
 * {@link MutableActionContext} - which are required to build how the Action will be scheduled.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied (can be {@code ?} for no actors).
 *
 * @see ActionEngine#createContext(Action)
 * @see ActionEngine#schedule(Action, Object)
 * @see ActionScheduler#scheduleAction(Action, long, long, java.util.concurrent.TimeUnit)
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Performs the actions using the supplied action context.
     *
     * @param context
     *            Current action context.
     *
     * @see Runnable#run()
     */
    void perform(ActionContext<T> context);
}
