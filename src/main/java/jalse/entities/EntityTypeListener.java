package jalse.entities;

/**
 * Listener for {@link Entity} type changes and death. Unique {@link EntityTypeEvent} will be
 * supplied for each trigger.
 *
 * @author Elliot Ford
 *
 * @see Entity
 *
 */
public interface EntityTypeListener {

    /**
     * Triggered on entity marked as type.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityMarkedAsType(final EntityTypeEvent event) {}

    /**
     * Triggered on entity unmarked as type.
     *
     * @param event
     *            The entity event for this trigger.
     */
    default void entityUnmarkedAsType(final EntityTypeEvent event) {}
}
