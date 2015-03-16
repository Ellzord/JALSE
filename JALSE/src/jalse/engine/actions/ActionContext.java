package jalse.engine.actions;

import jalse.engine.EngineBindings;

import java.util.UUID;

/**
 * ActionContext is supplied when running every {@link Action}. This provides contextual information
 * about the running Action and also the ability to cancel it. {@link ActionEngine} and
 * {@link EngineBindings} can also be accessed.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public interface ActionContext<T> {

    /**
     * Cancels the associated action.
     *
     * @return Whether the action was cancelled.
     */
    default boolean cancel() {
	return getEngine().cancel(getID());
    }

    /**
     * Gets the associated actor.
     *
     * @return Actor the action is associated to.
     */
    T getActor();

    /**
     * Gets the engine bindings.
     *
     * @return Engine bindings.
     */
    default EngineBindings getBindings() {
	return getEngine().getBindings();
    }

    /**
     * Gets the associated engine.
     *
     * @return Engine action is run from.
     */
    ActionEngine getEngine();

    /**
     * Gets the action ID.
     *
     * @return The ID the action was scheduled with.
     */
    UUID getID();

    /**
     * Gets the actions repeat period.
     *
     * @return Period.
     */
    long getPeriod();

    /**
     * Gets the current tick delta in nanoseconds.
     *
     * @return Current tick delta.
     */
    default long getTickDeltaAsNanos() {
	return getEngine().getTickInfo().getDeltaAsNanos();
    }

    /**
     * Checks whether the action is periodic.
     *
     * @return Whether the action is repeat scheduled.
     */
    default boolean isPeriodic() {
	return getPeriod() > 0;
    }
}
