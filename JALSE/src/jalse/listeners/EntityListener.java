package jalse.listeners;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

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
    void entityCreated(EntityEvent event);

    /**
     * Triggered on entity death.
     *
     * @param event
     *            The entity event for this trigger.
     */
    void entityKilled(EntityEvent event);

    /**
     * Triggered on entity being received.
     *
     * @param event
     *            The entity event for this trigger.
     */
    void entityReceived(EntityEvent event);

    /**
     * Triggered on entity transfer.
     *
     * @param event
     *            The entity event for this trigger.
     */
    void entityTransferred(EntityEvent event);
}
