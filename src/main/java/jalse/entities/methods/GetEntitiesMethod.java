package jalse.entities.methods;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.GetEntities;
import jalse.entities.functions.GetEntitiesFunction;

/**
 * This is used for mapping calls to:
 * <ul>
 * <li>{@link EntityContainer#getEntities()}</li>
 * <li>{@link EntityContainer#getEntitiesOfType(Class)}</li>
 * <li>{@link EntityContainer#getEntitiesAsType(Class)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see GetEntities
 * @see GetEntitiesFunction
 *
 */
public class GetEntitiesMethod implements EntityMethod {

    private final Class<? extends Entity> type;
    private final boolean ofType;
    private final Set<Supplier<UUID>> idSuppliers;
    private final boolean entityType;
    private final Predicate<Entity> idFilter;

    /**
     * Creates a get entities method.
     *
     * @param type
     *            Entity type.
     * @param idSuppliers
     *            Entity ID suppliers for filtering.
     * @param ofType
     *            Whether filtering of type.
     */
    public GetEntitiesMethod(final Class<? extends Entity> type, final Set<Supplier<UUID>> idSuppliers,
	    final boolean ofType) {
	this.type = Objects.requireNonNull(type);
	this.idSuppliers = Objects.requireNonNull(idSuppliers);
	entityType = Entity.class.equals(type);
	this.ofType = ofType;
	// Create filter
	idFilter = newIDFilter();
    }

    @Override
    public Set<Class<? extends Entity>> getDependencies() {
	return Collections.singleton(type);
    }

    /**
     * Gets entity ID suppliers.
     *
     * @return ID suppliers.
     */
    public Set<Supplier<UUID>> getIDSuppliers() {
	return idSuppliers;
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
	Set<? extends Entity> set;
	// Check is entity type.
	if (entityType) {
	    set = entity.getEntities();
	} else {
	    // Check of type
	    set = ofType ? entity.getEntitiesOfType(type) : entity.getEntitiesAsType(type);
	}
	// Filter set
	return idSuppliers.isEmpty() ? set : set.stream().filter(idFilter).collect(Collectors.toSet());
    }

    /**
     * Whether it is type filtering.
     *
     * @return Type filtering.
     */
    public boolean isOfType() {
	return ofType;
    }

    private Predicate<Entity> newIDFilter() {
	return e -> {
	    // No filtering on empty suppliers.
	    if (idSuppliers.isEmpty()) {
		return true;
	    }
	    boolean found = false;
	    for (final Supplier<UUID> idSupplier : idSuppliers) {
		// Check is ID
		if (e.getID().equals(idSupplier.get())) {
		    found = true;
		    break;
		}
	    }
	    return found;
	};
    }
}
