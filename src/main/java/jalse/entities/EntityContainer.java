package jalse.entities;

import static jalse.attributes.Attributes.EMPTY_ATTRIBUTECONTAINER;
import static jalse.entities.Entities.asType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jalse.attributes.AttributeContainer;
import jalse.misc.ListenerSet;

/**
 * This is an {@link Entity} collection. Entities can only be added to a container by creating them
 * and remove them by killing them. Entity creation and death can be listened for using
 * {@link EntityListener} and {@link EntityEvent}.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityContainer
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
     *
     * @see #streamEntities()
     */
    default Set<Entity> getEntities() {
	return streamEntities().collect(Collectors.toSet());
    }

    /**
     * Gets all the entities as the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities as the type.
     *
     * @see #streamEntitiesAsType(Class)
     */
    default <T extends Entity> Set<T> getEntitiesAsType(final Class<T> type) {
	return streamEntitiesAsType(type).collect(Collectors.toSet());
    }

    /**
     * Gets all the entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities marked with the type.
     *
     * @see #streamEntitiesOfType(Class)
     */
    default <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {
	return streamEntitiesOfType(type).collect(Collectors.toSet());
    }

    /**
     * Gets the entity with the specified ID.
     *
     * @param id
     *            Unique ID of the entity.
     * @return The entity matching the supplied id or null if none found.
     */
    Entity getEntity(final UUID id);

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
    default <T extends Entity> T getEntityAsType(final UUID id, final Class<T> type) {
	final Entity e = getEntity(id);
	return e != null ? asType(e, type) : null;
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
     * This is a convenience method for getting an entity (optional).
     *
     * @param id
     *            Unique ID of the entity.
     * @return Gets an Optional of the resulting entity or an empty Optional if it was not found.
     * @throws NullPointerException
     *             If the ID is null.
     */
    default Optional<Entity> getOptEntity(final UUID id) {
	return Optional.ofNullable(getEntity(id));
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
    default <T extends Entity> Optional<T> getOptEntityAsType(final UUID id, final Class<T> type) {
	return getOptEntity(id).map(e -> asType(e, type));
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
	return getEntity(id) != null;
    }

    /**
     * Checks whether the container contains a particular listener.
     *
     * @param listener
     *            The EntityListener to check for.
     * @return Whether the container contains the given EntityListener.
     */
    default boolean hasEntityListener(final EntityListener listener) {
	return getEntityListeners().contains(listener);
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
     */
    default Entity newEntity() {
	return newEntity(EMPTY_ATTRIBUTECONTAINER);
    }

    /**
     * Creates a new entity with a random ID.
     *
     * @param sourceContainer
     *            Source attribute container.
     *
     * @return The newly created entity's ID.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     */
    default Entity newEntity(final AttributeContainer sourceContainer) {
	return newEntity(UUID.randomUUID(), sourceContainer);
    }

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
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> T newEntity(final Class<T> type) {
	return newEntity(type, EMPTY_ATTRIBUTECONTAINER);
    }

    /**
     * Creates a new entity with a random ID. This entity is marked as the specified entity type and
     * then wrapped to it.
     *
     * @param type
     *            Entity type.
     * @param sourceContainer
     *            Source attribute container.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> T newEntity(final Class<T> type, final AttributeContainer sourceContainer) {
	return newEntity(UUID.randomUUID(), type, sourceContainer);
    }

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
     */
    default Entity newEntity(final UUID id) {
	return newEntity(id, EMPTY_ATTRIBUTECONTAINER);
    }

    /**
     * Creates new entity with the specified ID.
     *
     * @param id
     *            Entity ID.
     * @param sourceContainer
     *            Source attribute container.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     */
    Entity newEntity(UUID id, AttributeContainer sourceContainer);

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
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	return newEntity(id, type, EMPTY_ATTRIBUTECONTAINER);
    }

    /**
     * Creates new entity with the specified ID. This entity is marked as the specified entity type
     * and then wrapped to it.
     *
     *
     * @param id
     *            Entity ID.
     * @param type
     *            Entity type.
     * @param sourceContainer
     *            Source attribute container.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     *
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     */
    <T extends Entity> T newEntity(UUID id, Class<T> type, AttributeContainer sourceContainer);

    /**
     * Receives an entity (from a transfer). This method may receive an entity from within or
     * outside the tree.
     *
     * @param e
     *            Entity to receive.
     * @return Whether the entity was received.
     *
     * @see #transferEntity(UUID, EntityContainer)
     */
    boolean receiveEntity(Entity e);

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
     * Removes all listeners for entities.
     */
    void removeEntityListeners();

    /**
     * Provides a stream of entities from the container.
     *
     * @return A stream of entities in the container.
     */
    Stream<Entity> streamEntities();

    /**
     * Gets a stream of as the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Stream of entities as the type.
     *
     * @see Entity#asType(Class)
     * @see #streamEntities()
     */
    default <T extends Entity> Stream<T> streamEntitiesAsType(final Class<T> type) {
	return streamEntities().map(e -> asType(e, type));
    }

    /**
     * Gets a stream of entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Stream of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     * @see #streamEntities()
     */
    default <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return streamEntities().filter(e -> e.isMarkedAsType(type)).map(e -> asType(e, type));
    }

    /**
     * Streams IDs of all the entities within the container.
     *
     * @return Stream of all entity identifiers.
     */
    default Stream<UUID> streamEntityIDs() {
	return getEntityIDs().stream();
    }

    /**
     * Transfers all entities to the destination.
     *
     * @param destination
     *            Destination to transfer to.
     * @return Entities that could not be transferred.
     *
     * @see #transferEntities(Set, EntityContainer)
     */
    default Set<UUID> transferAllEntities(final EntityContainer destination) {
	return transferEntities(getEntityIDs(), destination);
    }

    /**
     * Transfers all entities to the destination.
     *
     * @param predicate
     *            Predicate to filter entities.
     * @param destination
     *            Destination to transfer to.
     * @return Entities that could not be transferred.
     */
    default Set<UUID> transferEntities(final Predicate<Entity> predicate, final EntityContainer destination) {
	return transferEntities(streamEntities().filter(predicate).map(Entity::getID).collect(Collectors.toSet()),
		destination);
    }

    /**
     * Transfers a number of entities
     *
     * @param entityIDs
     *            Entities to transfer.
     * @param destination
     *            Destination to transfer to.
     * @return Entities that could not be transferred.
     */
    default Set<UUID> transferEntities(final Set<UUID> entityIDs, final EntityContainer destination) {
	Objects.requireNonNull(destination);

	final Set<UUID> notTransferred = new HashSet<>();
	for (final UUID id : entityIDs) {
	    if (!transferEntity(id, destination)) {
		notTransferred.add(id);
	    }
	}
	return notTransferred;
    }

    /**
     * Transfers the entity to the supplied destination container.
     *
     * @param id
     *            Entity ID.
     * @param destination
     *            Target container.
     * @return Whether the entity was transferred.
     *
     * @see #receiveEntity(Entity)
     */
    boolean transferEntity(UUID id, EntityContainer destination);
}
