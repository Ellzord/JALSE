package jalse.listeners;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

/**
 * Listener for {@link Entity} creation and death. Unique {@link EntityContainerEvent} will be
 * supplied for each trigger.
 *
 * @author Elliot Ford
 *
 * @see EntityContainer
 *
 */
public interface EntityContainerListener {

    /**
     * Triggered on entity creation.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityCreated(final EntityContainerEvent event) {}

    /**
     * Triggered on entity death.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityKilled(final EntityContainerEvent event) {}

    /**
     * Triggered on entity being received.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityReceived(final EntityContainerEvent event) {}

    /**
     * Triggered on entity transfer.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityTransferred(final EntityContainerEvent event) {}
}
