package jalse.tags;

import jalse.entities.Entities;
import jalse.entities.Entity;

/**
 * Tag used to help identify an {@link Entity} marked by type.
 *
 * @author Elliot Ford
 *
 */
public class EntityType implements Tag {

    private final Class<? extends Entity> type;

    /**
     * Creates a new entity type tag.
     *
     * @param type
     *            Entity type
     * @throws IllegalArgumentException
     *
     * @see Entities#validateType(Class)
     */
    public EntityType(final Class<? extends Entity> type) {

	Entities.validateType(type);

	this.type = type;
    }

    @Override
    public boolean equals(final Object obj) {

	return obj instanceof EntityType && type.equals(((EntityType) obj).type);
    }

    /**
     * Gets the entity type.
     *
     * @return Type belonging to an entity.
     */
    public Class<? extends Entity> getType() {

	return type;
    }

    @Override
    public int hashCode() {

	return type.hashCode();
    }

    @Override
    public String toString() {

	return "EntityType [type=" + type.getName() + "]";
    }
}
