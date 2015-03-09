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
     * Triggered on cluster creation.
     *
     * @param event
     *            The cluster event for this trigger.
     */
    void entityCreated(EntityEvent event);

    /**
     * Triggered on cluster death.
     *
     * @param event
     *            The cluster event for this trigger.
     */
    void entityKilled(EntityEvent event);

}
