package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.GetEntity;
import jalse.entities.functions.GetEntityFunction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is used for mapping calls to:
 * <ul>
 * <li>{@link EntityContainer#getEntityAsType(UUID, Class)}</li>
 * <li>{@link EntityContainer#getOptEntityAsType(UUID, Class)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see GetEntity
 * @see GetEntityFunction
 *
 */
public class GetEntityMethod implements EntityMethod {

    private final Class<? extends Entity> type;
    private final Supplier<UUID> idSupplier;
    private final boolean optional;
    private final boolean entityType;

    /**
     * Creates a get entity method.
     *
     * @param type
     *            Entity type.
     * @param optional
     *            Whether is opt.
     * @param idSupplier
     *            Entity ID supplier.
     * @param idParam
     *            Whether the ID is required as param.
     */
    public GetEntityMethod(final Class<? extends Entity> type, final boolean optional, final Supplier<UUID> idSupplier) {
	this.type = Objects.requireNonNull(type);
	this.optional = optional;
	this.idSupplier = idSupplier;
	entityType = Entity.class.equals(type);
    }

    @Override
    public Set<Class<? extends Entity>> getDependencies() {
	return Collections.singleton(type);
    }

    /**
     * Gets entity ID supplier.
     *
     * @return ID supplier.
     */
    public Supplier<UUID> getIDSupplier() {
	return idSupplier;
    }

    /**
     * Gets the entity type.
     *
     * @return Entity return type.
     */
    public Class<? extends Entity> getType() {
	return type;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Get entity ID
	final UUID idArg = idSupplier != null ? idSupplier.get() : (UUID) args[0];
	// Check optional
	if (optional) {
	    return entityType ? entity.getOptEntity(idArg) : entity.getOptEntityAsType(idArg, type);
	} else {
	    return entityType ? entity.getEntity(idArg) : entity.getEntityAsType(idArg, type);
	}
    }

    /**
     * Checks whether this is an opt method.
     *
     * @return Opt method.
     */
    public boolean isOptional() {
	return optional;
    }
}
