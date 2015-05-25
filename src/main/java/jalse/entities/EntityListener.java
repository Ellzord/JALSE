package jalse.entities;

/**
 * Listener for {@link Entity} creation and death. Unique {@link EntityEvent} will be supplied for
 * each trigger.
 *
 * @author Elliot Ford
 *
 * @see EntityContainer
 *
 */
public interface EntityListener {

    /**
     * Triggered on entity creation.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityCreated(final EntityEvent event) {}

    /**
     * Triggered on entity death.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityKilled(final EntityEvent event) {}

    /**
     * Triggered on entity being received.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityReceived(final EntityEvent event) {}

    /**
     * Triggered on entity transfer.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityTransferred(final EntityEvent event) {}
}
