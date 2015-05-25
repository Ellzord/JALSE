package jalse.entities;

import jalse.actions.ActionScheduler;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeContainerListener;
import jalse.attributes.AttributeType;
import jalse.misc.Identifiable;
import jalse.misc.ListenerSet;
import jalse.tags.Taggable;

import java.util.Optional;
import java.util.Set;

/**
 * Entity plays the greatest role in the overall data model. An entity is representative of a single
 * entity or group of entities with a defined identities. Entities have {@link AttributeType} as
 * well as {@link AttributeContainerListener} for trigger code upon add, removal or change of those
 * attributes. Entities can create and kill other entities (tree-like structure) these events can be
 * accessed by adding {@link EntityContainerListener}. <br>
 * <br>
 * Entities can be wrapped and marked as specific entity types as long as the inheriting interface
 * follows what is outlined in {@link Entities}. Entity have can have a number of types (subclasses
 * of {@link Entity}) which can be used to identify a collection of entities with similar state or
 * function.<br>
 * <br>
 * An example of how the type marking works:
 *
 * <pre>
 * <code>
 * public interface Animal extends Entity {}
 * public interface FlyingAnimal extends Animal {}
 * 
 * Entity e; // Previously created entity.
 * e.markAsType(FlyingAnimal.class);
 * 
 * assert e.isMarkedAsType(Animal.class);
 * </code>
 * </pre>
 *
 * NOTE: Taking an entity {@link #asType(Class)} is similar to casting but does not mark as the
 * entity with the type.
 *
 *
 * @author Elliot Ford
 *
 * @see EntityContainer
 * @see DefaultEntityContainer
 * @see EntityContainerListener
 * @see EntityFactory
 * @see Entities#asType(Entity, Class)
 *
 */
public interface Entity extends EntityContainer, Identifiable, AttributeContainer, Taggable, ActionScheduler<Entity> {

    /**
     * Adds a listener for the entity.
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
     * Convenience method for wrapping the entity to a different type.
     *
     * @param type
     *            Entity type to wrap to.
     * @return The wrapped entity.
     *
     * @see Entities#asType(Entity, Class)
     */
    default <T extends Entity> T asType(final Class<T> type) {
	return Entities.asType(this, type);
    }

    /**
     * Gets the parent container.
     *
     * @return The parent container or null if not found.
     */
    EntityContainer getContainer();

    /**
     * Gets all the entity listeners.
     *
     * @return All the entity listeners.
     */
    Set<? extends EntityListener> getEntityListeners();

    /**
     * Gets the types this entity has been marked as.
     *
     * @return Marked types.
     */
    Set<Class<? extends Entity>> getMarkedTypes();

    /**
     * This is a convenience method for getting the container (optional).
     *
     * @return Optional containing the container or else empty optional if the entity is not alive.
     */
    default Optional<EntityContainer> getOptContainer() {
	return Optional.ofNullable(getContainer());
    }

    /**
     * Checks whether there is an associated container.
     *
     * @return Whether there was a container.
     */
    default boolean hasContainer() {
	return getContainer() != null;
    }

    /**
     * Checks whether the container contains a particular listener.
     *
     * @param listener
     *            The EntityContainerListener to check for.
     * @return Whether the container contains the given EntityContainerListener.
     */
    default boolean hasEntityListener(final EntityListener listener) {
	return getEntityListeners().contains(listener);
    }

    /**
     * Checks if the entity is alive.
     *
     * @return Whether the entity is alive.
     */
    boolean isAlive();

    /**
     * Checks whether the entity has the associated type.
     *
     * @param type
     *            Entity type to check.
     * @return Whether the entity was previously associated to the type.
     */
    boolean isMarkedAsType(Class<? extends Entity> type);

    /**
     * Kills the entity.
     *
     * @return Whether the entity was alive.
     */
    boolean kill();

    /**
     * Adds the specified type to the entity. If any of the ancestry of this type are not associated
     * to this entity they will also be added.
     *
     * @param type
     *            Entity type to add.
     * @return Whether the type was not associated to the entity.
     */
    boolean markAsType(Class<? extends Entity> type);

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
     * Transfers this entity to the specified destination container.
     *
     * @param destination
     *            Target container.
     * @return Whether the entity was transfered.
     *
     */
    boolean transfer(EntityContainer destination);

    /**
     * Removes the specified type from the entity. If this type is the ancestor of any other types
     * associated to the entity they will be removed.
     *
     * @param type
     *            Entity type to remove.
     * @return Whether the entity was previously associated to the type (or its any of its
     *         children).
     */
    boolean unmarkAsType(Class<? extends Entity> type);
}
