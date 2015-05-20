package jalse.listeners;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.Optional;
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
    private final Class<? extends Entity> typeChange;
    private final EntityContainer destination;

    /**
     * Creates a new EntityEvent with a random ID.
     *
     * @param container
     *            Parent container for the Entity.
     * @param entity
     *            Entity the event is for.
     */
    public EntityEvent(final EntityContainer container, final Entity entity) {
	this(container, entity, null, null);
    }

    /**
     * Creates a new EntityEvent with a random ID.
     *
     * @param container
     *            Parent container for the Entity.
     * @param entity
     *            Entity the event is for.
     * @param typeChange
     *            The type the entity was changed to.
     */
    public EntityEvent(final EntityContainer container, final Entity entity, final Class<? extends Entity> typeChange) {
	this(container, entity, typeChange, null);
    }

    /**
     * Creates a new EntityEvent with a random ID.
     *
     * @param container
     *            Parent container for the entity.
     * @param entity
     *            Entity the event is for.
     * @param typeChange
     *            The type the entity was changed to.
     * @param destination
     *            Destination container for transfer events (can be null).
     */
    public EntityEvent(final EntityContainer container, final Entity entity, final Class<? extends Entity> typeChange,
	    final EntityContainer destination) {
	super(UUID.randomUUID());
	this.container = Objects.requireNonNull(container);
	this.entity = Objects.requireNonNull(entity);
	this.typeChange = typeChange;
	this.destination = destination;
    }

    /**
     * Creates a new EntityEvent with a random ID.
     *
     * @param container
     *            Parent container for the entity.
     * @param entity
     *            Entity the event is for.
     * @param destination
     *            Destination container for transfer events (can be null).
     */
    public EntityEvent(final EntityContainer container, final Entity entity, final EntityContainer destination) {
	this(container, entity, null, destination);
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
     * Get the destination container for the transfer event.
     *
     * @return Destination container or null if there was not one.
     */
    public EntityContainer getDestinationContainer() {
	return destination;
    }

    /**
     * Gets the event entity.
     *
     * @return The entity the event was triggered for.
     */
    public Entity getEntity() {
	return entity;
    }

    /**
     * Get the destination container for the transfer event.
     *
     * @return Optional containing the destination container or empty optional if nothing was
     *         transferred.
     */
    public Optional<EntityContainer> getOptDestinationContainer() {
	return Optional.ofNullable(destination);
    }

    /**
     * Get the type the entity was changed to.
     *
     * @return Optional containing the type the entity was changed to or empty optional if the type
     *         was not changed.
     */
    public Optional<Class<? extends Entity>> getOptTypeChange() {
	return Optional.ofNullable(typeChange);
    }

    /**
     * Get the type the entity was changed to.
     *
     * @return The type the entity was changed to.
     */
    public Class<? extends Entity> getTypeChange() {
	return typeChange;
    }

    /**
     * Checks whether this is a transfer event.
     *
     * @return Whether there is a destination container.
     */
    public boolean isTransfer() {
	return destination != null;
    }

    /**
     * Checks whether this is a type change event.
     *
     * @return Whether the type was changed.
     */
    public boolean isTypeChange() {
	return getTypeChange() != null;
    }

}
