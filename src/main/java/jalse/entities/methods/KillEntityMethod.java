package jalse.entities.methods;

import java.util.UUID;
import java.util.function.Supplier;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.KillEntity;
import jalse.entities.functions.KillEntityFunction;

/**
 * This is used for mapping calls to {@link EntityContainer#killEntity(UUID)}.
 *
 * @author Elliot Ford
 *
 * @see KillEntity
 * @see KillEntityFunction
 *
 */
public class KillEntityMethod implements EntityMethod {

    private Supplier<UUID> idSupplier;

    /**
     * Creates a new kill entity method.
     */
    public KillEntityMethod() {
	idSupplier = null;
    }

    /**
     * Gets the entity ID supplier.
     *
     * @return ID supplier.
     */
    public Supplier<UUID> getIDSupplier() {
	return idSupplier;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Entity ID
	final UUID id = idSupplier != null ? idSupplier.get() : (UUID) args[0];
	// Kill
	return entity.killEntity(id);
    }

    /**
     * Sets the entity ID supplier.
     *
     * @param idSupplier
     *            ID supplier.
     */
    public void setIDSupplier(final Supplier<UUID> idSupplier) {
	this.idSupplier = idSupplier;
    }
}
