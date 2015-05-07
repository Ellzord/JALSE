package jalse.entities;

import jalse.actions.ActionEngine;

import java.util.UUID;

/**
 * An entity factory is used to control the creation, transfer and killing of {@link Entity} within
 * the entire tree.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityContainer
 * @see DefaultEntityFactory
 *
 */
public interface EntityFactory {

    /**
     * Exports an entity (removing all references).
     *
     * @param e
     *            Entity to detach.
     */
    void exportEntity(Entity e);

    /**
     * Creates a new entity with the specified ID and parent container.
     *
     * @param id
     *            Entity ID.
     * @param target
     *            Parent container.
     * @return Newly created entity.
     */
    Entity newEntity(UUID id, EntityContainer target);

    /**
     * Sets the engine to supply to new entities.
     *
     * @param engine
     *            Action engine.
     */
    void setEngine(ActionEngine engine);

    /**
     * Imports the entity into the factory.
     *
     * @param e
     *            Entity to import.
     * @param target
     *            Target container.
     * @return Whether the entity was imported.
     */
    boolean tryImportEntity(Entity e, EntityContainer target);

    /**
     * Kills the specified entity.
     *
     * @param e
     *            Entity to kill.
     * @return Whether the entity was killed.
     */
    boolean tryKillEntity(Entity e);

    /**
     * Tries to take the entity from within the tree if possible.
     *
     * @param e
     *            Entity to take.
     * @param target
     *            Target container.
     * @return Whether the entity could be taken from within the tree.
     */
    boolean tryTakeFromTree(Entity e, EntityContainer target);

    /**
     * Checks if the two containers are within the same tree (for in tree transfer or export).
     *
     * @param source
     *            Source container.
     * @param target
     *            Target container.
     * @return Whether they're within the same tree.
     */
    boolean withinSameTree(EntityContainer source, EntityContainer target);
}
