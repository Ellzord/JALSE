package jalse.entities.methods;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.KillEntities;
import jalse.entities.functions.KillEntitiesFunction;

/**
 * This is used for mapping calls to {@link EntityContainer#killEntities()}.
 *
 * @author Elliot Ford
 *
 * @see KillEntities
 * @see KillEntitiesFunction
 *
 */
public class KillEntitiesMethod implements EntityMethod {

    private final Set<Supplier<UUID>> idSuppliers;

    /**
     * Creates a kill entities method.
     *
     * @param idSuppliers
     *            Entity ID suppliers for filtering.
     */
    public KillEntitiesMethod(final Set<Supplier<UUID>> idSuppliers) {
	this.idSuppliers = Objects.requireNonNull(idSuppliers);
    }

    /**
     * Gets entity ID suppliers.
     *
     * @return ID suppliers.
     */
    public Set<Supplier<UUID>> getIDSuppliers() {
	return idSuppliers;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Kill all
	if (idSuppliers.isEmpty()) {
	    entity.killEntities();
	}
	// Kill selected
	else {
	    idSuppliers.stream().map(Supplier::get).forEach(entity::killEntity);
	}
	// void return
	return null;
    }
}
