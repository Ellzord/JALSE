package jalse.actions;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ActionContext is supplied when running every {@link Action}. This provides contextual information
 * about the running Action and also the ability to cancel it.<br>
 * <br>
 * Key-value pairs may be bound for this context to be used when performing the action.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type (can be {@code ?} if no actor).
 */
public interface ActionContext<T> extends ActionBindings {

    /**
     * Cancels the associated action.
     *
     * @return Whether the action was cancelled.
     */
    boolean cancel();

    /**
     * Gets the associated actor.
     *
     * @return The referenced actor or null if not found.
     */
    T getActor();

    /**
     * Gets the associated engine.
     *
     * @return Engine action is scheduled by from.
     */
    ActionEngine getEngine();

    /**
     * This is a convenience method for getting an actor (optional).
     *
     * @return Optional containing the referenced actor or else empty optional if not found.
     */
    default Optional<T> getOptActor() {
	return Optional.ofNullable(getActor());
    }

    /**
     * Gets the actions repeat period.
     *
     * @param unit
     *            TimeUnit to convert period to.
     *
     * @return Period in the supplied unit.
     */
    long getPeriod(TimeUnit unit);

    /**
     * Whether the context has an associated actor.
     *
     * @return Whether an actor was found.
     */
    default boolean hasActor() {
	return getActor() != null;
    }

    /**
     * Whether the action has been cancelled.
     *
     * @return Cancelled state.
     */
    boolean isCancelled();

    /**
     * Whether the action has been ran (or cancelled).
     *
     * @return Done state.
     */
    boolean isDone();

    /**
     * Checks whether the action is periodic.
     *
     * @return Whether the action is repeat scheduled.
     */
    default boolean isPeriodic() {
	return getPeriod(TimeUnit.NANOSECONDS) > 0;
    }
}
