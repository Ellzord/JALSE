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
     * @return Whether the entity was exported.
     */
    boolean exportEntity(Entity e);

    /**
     * Imports the entity into the container.
     *
     * @param e
     *            Entity to import.
     * @param container
     *            Target container.
     * @return Whether the entity can be imported.
     */
    boolean importEntity(Entity e, EntityContainer container);

    /**
     * Kills the specified entity.
     *
     * @param e
     *            Entity to kill.
     * @return Whether the entity was killed.
     */
    boolean killEntity(Entity e);

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
}
