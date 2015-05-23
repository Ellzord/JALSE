package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.StreamEntities;
import jalse.entities.functions.StreamEntitiesFunction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is used for mapping calls to:
 * <ul>
 * <li>{@link EntityContainer#streamEntities()}</li>
 * <li>{@link EntityContainer#streamEntitiesOfType(Class)}</li>
 * <li>{@link EntityContainer#streamEntitiesAsType(Class)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see StreamEntities
 * @see StreamEntitiesFunction
 *
 */
public class StreamEntitiesMethod implements EntityMethod {

    private final Class<? extends Entity> type;
    private final boolean ofType;
    private final Set<Supplier<UUID>> idSuppliers;
    private final boolean entityType;
    private final Predicate<Entity> idFilter;

    /**
     * Creates a stream entities method.
     *
     * @param type
     *            Entity type.
     * @param idSuppliers
     *            Entity ID suppliers for filtering.
     * @param ofType
     *            Whether filtering of type.
     */
    public StreamEntitiesMethod(final Class<? extends Entity> type, final Set<Supplier<UUID>> idSuppliers,
	    final boolean ofType) {
	this.type = Objects.requireNonNull(type);
	this.ofType = ofType;
	this.idSuppliers = Objects.requireNonNull(idSuppliers);
	entityType = Entity.class.equals(type);
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
	Stream<? extends Entity> stream;
	// Check is entity type.
	if (entityType) {
	    stream = entity.streamEntities();
	} else {
	    // Check of type
	    stream = ofType ? entity.streamEntitiesOfType(type) : entity.streamEntitiesAsType(type);
	}
	// Filter stream
	return idSuppliers.isEmpty() ? stream : stream.filter(idFilter);
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
	    for (final Supplier<UUID> idSupplier : idSuppliers) {
		// Check is ID
		if (e.getID().equals(idSupplier.get())) {
		    return true;
		}
	    }
	    // Not found
	    return false;
	};
    }
}
