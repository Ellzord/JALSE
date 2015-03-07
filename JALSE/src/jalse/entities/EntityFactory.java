package jalse.entities;

import java.util.UUID;

/**
 * An entity factory is used to give a level of control over the creation and
 * killing of {@link Entity}.
 *
 * @author Elliot Ford
 *
 * @see EntitySet
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
}
