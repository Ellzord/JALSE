package jalse.entities;

import jalse.actions.ActionEngine;

import java.util.UUID;

/**
 * An entity factory is used to control the creation and killing of {@link Entity} within the entire
 * tree.
 *
 * @author Elliot Ford
 *
 * @see EntitySet
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
     * @param container
     *            Parent container.
     * @return Newly created entity.
     */
    Entity newEntity(UUID id, EntityContainer container);

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
     * @param container
     *            Target container.
     * @return Whether the entity was imported.
     */
    boolean tryImportEntity(Entity e, EntityContainer container);

    /**
     * Kills the specified entity.
     *
     * @param e
     *            Entity to kill.
     * @return Whether the entity was killed.
     */
    boolean tryKillEntity(Entity e);

    /**
     * If applicable will move the entity within the tree (transfer).
     *
     * @param e
     *            Entity to move.
     * @param container
     *            Container to become new parent.
     * @return Whether the move was possible.
     */
    boolean tryMoveWithinTree(Entity e, EntityContainer container);
}
