package jalse.entities;

import static jalse.entities.Entities.asType;
import jalse.listeners.EntityEvent;
import jalse.listeners.EntityListener;
import jalse.listeners.ListenerSet;
import jalse.misc.JALSEExceptions;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This is an {@link Entity} collection. Entities can only be added to a container by creating them
 * and remove them by killing them. Entity creation and death can be listened for using
 * {@link EntityListener} and {@link EntityEvent}.
 *
 * @author Elliot Ford
 *
 * @see EntitySet
 * @see EntityFactory
 * @see Entities#emptyEntityContainer()
 * @see Entities#unmodifiableEntityContainer(EntityContainer)
 */
public interface EntityContainer {

    /**
     * Adds a listener for entities.
     *
     * @param listener
     *            Listener to add.
     *
     * @return {@code true} if container did not already contain this listener.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#add(Object)
     *
     */
    boolean addEntityListener(EntityListener listener);

    /**
     * Gets all the entities within the containers.
     *
     * @return Gets all entities or an empty set if none were found.
     */
    Set<Entity> getEntities();

    /**
     * Gets all the entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     */
    <T extends Entity> Set<T> getEntitiesOfType(Class<T> type);

    /**
     * This is a convenience method for getting an entity (optional).
     *
     * @param id
     *            Unique ID of the entity.
     * @return Gets an Optional of the resulting entity or an empty Optional if it was not found.
     * @throws NullPointerException
     *             If the ID is null.
     */
    default Optional<Entity> getEntity(final UUID id) {
	return Optional.ofNullable(getOrNullEntity(id));
    }

    /**
     * This is a convenience method for getting an entity (optional).The entity is wrapped with the
     * supplied entity type.
     *
     * @param id
     *            Unique ID of the entity.
     * @param type
     *            Entity type to wrap to.
     * @return Gets an Optional of the resulting entity or an empty Optional if it was not found.
     * @throws NullPointerException
     *             If type is null.
     *
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> Optional<T> getEntityAsType(final UUID id, final Class<T> type) {
	return getEntity(id).map(e -> asType(e, type));
    }

    /**
     * Gets the direct entity count.
     *
     * @return Direct child entity count.
     */
    int getEntityCount();

    /**
     * Gets the IDs of all the entities within the container.
     *
     * @return Set of all entity identifiers.
     */
    Set<UUID> getEntityIDs();

    /**
     * Gets all the entity listeners.
     *
     * @return All the entity listeners.
     */
    Set<? extends EntityListener> getEntityListeners();

    /**
     * Gets the entity with the specified ID.
     *
     * @param id
     *            Unique ID of the entity.
     * @return The entity matching the supplied id or null if none found.
     */
    Entity getOrNullEntity(final UUID id);

    /**
     * This is a convenience method for getting an entity (no optional). The entity is wrapped with
     * the supplied entity type.
     *
     * @param id
     *            Unique ID of the entity.
     * @param type
     *            Entity type to wrap to.
     * @return The entity matching the supplied id or null if none found.
     *
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> T getOrNullEntityAsType(final UUID id, final Class<T> type) {
	final Entity e = getOrNullEntity(id);
	return e != null ? asType(e, type) : null;
    }

    /**
     * Checks whether the container has any entities.
     *
     * @return Whether the container is not empty.
     */
    default boolean hasEntities() {
	return getEntityCount() > 0;
    }

    /**
     * Checks whether the entity is contained.
     *
     * @param id
     *            Entity ID.
     * @return Whether the entity was found.
     */
    default boolean hasEntity(final UUID id) {
	return getEntity(id).isPresent();
    }

    /**
     * Kills all entities.
     */
    void killEntities();

    /**
     * Kills the specified entity.
     *
     * @param id
     *            Entity ID.
     * @return Whether the entity was alive.
     */
    boolean killEntity(UUID id);

    /**
     * Creates a new entity with a random ID.
     *
     * @return The newly created entity's ID.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     */
    Entity newEntity();

    /**
     * Creates a new entity with a random ID. This entity is marked as the specified entity type and
     * then wrapped to it.
     *
     * @param type
     *            Entity type.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    <T extends Entity> T newEntity(Class<T> type);

    /**
     * Creates new entity with the specified ID.
     *
     * @param id
     *            Entity ID.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     *
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see JALSEExceptions#ENTITY_ALREADY_ASSOCIATED
     */
    Entity newEntity(UUID id);

    /**
     * Creates new entity with the specified ID. This entity is marked as the specified entity type
     * and then wrapped to it.
     *
     *
     * @param id
     *            Entity ID.
     * @param type
     *            Entity type.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     *
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see JALSEExceptions#ENTITY_ALREADY_ASSOCIATED
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    <T extends Entity> T newEntity(UUID id, Class<T> type);

    /**
     * Removes a entity listener.
     *
     * @param listener
     *            Listener to remove.
     *
     * @return {@code true} if the listener was removed.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#remove(Object)
     *
     */
    boolean removeEntityListener(EntityListener listener);

    /**
     * Provides a stream of entities from the container.
     *
     * @return A stream of entities in the container.
     */
    Stream<Entity> streamEntities();

    /**
     * Gets a stream of entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Stream of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     */
    <T extends Entity> Stream<T> streamEntitiesOfType(Class<T> type);
}
