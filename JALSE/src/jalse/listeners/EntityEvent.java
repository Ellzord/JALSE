package jalse.listeners;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity change event for {@link EntityListener}. This is a unique event that contains the relevant
 * {@link Entity} and it's parent {@link EntityContainer} (even if the Entity has been killed).
 *
 * @author Elliot Ford
 *
 * @see EntityContainer
 *
 */
public class EntityEvent extends AbstractIdentifiable {

    private final Entity entity;
    private final EntityContainer container;

    /**
     * Creates a new EntityEvent with a random ID.
     *
     * @param container
     *            Parent container for the Entity.
     * @param entity
     *            Entity the event is for.
     */
    public EntityEvent(final EntityContainer container, final Entity entity) {
	super(UUID.randomUUID());
	this.container = Objects.requireNonNull(container);
	this.entity = Objects.requireNonNull(entity);
    }

    /**
     * Gets the Entity's parent container.
     *
     * @return Entity's parent container.
     */
    public EntityContainer getContainer() {
	return container;
    }

    /**
     * Gets the event entity.
     *
     * @return The entity the event was triggered for.
     */
    public Entity getEntity() {
	return entity;
    }
}
