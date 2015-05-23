package jalse.entities.methods;

import jalse.attributes.AttributeContainer;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.NewEntity;
import jalse.entities.functions.NewEntityFunction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is used for mapping calls to:
 * <ul>
 * <li>{@link EntityContainer#newEntity()}</li>
 * <li>{@link EntityContainer#newEntity(UUID)}</li>
 * <li>{@link EntityContainer#newEntity(Class)}</li>
 * <li>{@link EntityContainer#newEntity(AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, Class)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(Class, AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, Class, AttributeContainer)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see NewEntity
 * @see NewEntityFunction
 *
 */
public class NewEntityMethod implements EntityMethod {

    private final Class<? extends Entity> type;
    private final boolean entityType;
    private Supplier<UUID> idSupplier;
    private boolean idParam;
    private boolean containerParam;

    /**
     * Creates a new entity method.
     *
     * @param type
     *            Entity type.
     */
    public NewEntityMethod(final Class<? extends Entity> type) {
	this.type = Objects.requireNonNull(type);
	entityType = Entity.class.equals(type);
	idParam = false;
	containerParam = false;
	idSupplier = null;
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
	// ID and container
	if (idParam && containerParam || idSupplier != null && containerParam) {
	    // Work out ID and adjust params
	    final UUID idArg = idParam ? (UUID) args[0] : idSupplier.get();
	    final AttributeContainer conArg = (AttributeContainer) args[idParam ? 1 : 0];
	    // Check type
	    return entityType ? entity.newEntity(idArg, conArg) : entity.newEntity(idArg, type, conArg);
	}
	// Container
	else if (containerParam) {
	    final AttributeContainer conArg = (AttributeContainer) args[0];
	    // Check type
	    return entityType ? entity.newEntity(conArg) : entity.newEntity(type, conArg);
	}
	// ID
	else if (idParam || idSupplier != null) {
	    // Work out ID
	    final UUID idArg = idParam ? (UUID) args[0] : idSupplier.get();
	    // Check type
	    return entityType ? entity.newEntity(idArg) : entity.newEntity(idArg, type);
	}
	// No params
	else {
	    // Check type
	    return entityType ? entity.newEntity() : entity.newEntity(type);
	}
    }

    /**
     * Checks whether the container is required as param.
     *
     * @return Container required.
     */
    public boolean requiresContainerParam() {
	return containerParam;
    }

    /**
     * Checks whether the ID is required as param.
     *
     * @return ID required.
     */
    public boolean requiresIDParam() {
	return idParam;
    }

    /**
     * Set the entity ID supplier.
     *
     * @param idSupplier
     *            Entity ID supplier.
     */
    public void setIDSupplier(final Supplier<UUID> idSupplier) {
	this.idSupplier = idSupplier;
    }

    /**
     * Set whether the method requires container param.
     *
     * @param containerParam
     *            Whether the container is required.
     */
    public void setRequiresContainerParam(final boolean containerParam) {
	this.containerParam = containerParam;
    }

    /**
     * Set whether the method requires ID param.
     *
     * @param idParam
     *            Whether the ID param is required.
     */
    public void setRequiresIDParam(final boolean idParam) {
	this.idParam = idParam;
    }
}
