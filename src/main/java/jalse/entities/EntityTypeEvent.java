package jalse.entities;

import jalse.misc.AbstractIdentifiable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Entity change event for {@link EntityTypeListener}. This is a unique event that contains the
 * relevant {@link Entity}.
 *
 * @author Elliot Ford
 *
 * @see Entity
 *
 */
public class EntityTypeEvent extends AbstractIdentifiable {

    private final Entity entity;
    private final Class<? extends Entity> typeChange;
    private final Set<Class<? extends Entity>> typeChangeDependants;

    /**
     * Creates a new EntityTypeEvent with a random ID.
     *
     * @param entity
     *            Entity the event is for.
     * @param typeChange
     *            Entity type change.
     * @param typeChangeDependants
     *            Addtional types that were changed due to to this event.
     */
    public EntityTypeEvent(final Entity entity, final Class<? extends Entity> typeChange,
	    final Set<Class<? extends Entity>> typeChangeDependants) {
	this.entity = Objects.requireNonNull(entity);
	this.typeChange = Objects.requireNonNull(typeChange);
	this.typeChangeDependants = Objects.requireNonNull(typeChangeDependants);
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
     * Gets the event type change.
     *
     * @return The type change of the triggering event.
     */
    public Class<? extends Entity> getTypeChange() {
	return typeChange;
    }

    public Set<Class<? extends Entity>> getTypeChangeDependants() {
	return Collections.unmodifiableSet(typeChangeDependants);
    }
}
