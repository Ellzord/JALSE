package jalse.entities;

import jalse.engine.actions.ActionEngine;

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
