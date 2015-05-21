package jalse.entities.functions;

import static jalse.entities.Entities.isEntityOrSubtype;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.returnTypeIs;
import static jalse.entities.functions.Functions.toClass;
import static jalse.entities.functions.Functions.toIDSupplier;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.StreamEntities;
import jalse.entities.methods.StreamEntitiesMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is a method function for {@link StreamEntities} annotation. It will resolve an
 * {@link StreamEntitiesMethod} to be used by the entity typing system. This differs from the usual
 * streaming of {@link Entity} as it also lets you stream a subset of {@link Entity} using
 * {@link EntityID} (so adding to any method will cause it to filter).<br>
 * <br>
 * The next example signature will resolve to {@link EntityContainer#streamEntities()}.
 *
 * <pre>
 * <code>
 * {@code @StreamEntities}
 * {@code Stream<Entity>} streamGhosts();
 * </code>
 * </pre>
 *
 * The next example signature will resolve to {@link EntityContainer#streamEntitiesOfType(Class)}.
 *
 * <pre>
 * <code>
 * {@code @StreamEntities}
 * {@code Stream<Ghost>} streamGhosts();
 * </code>
 * </pre>
 *
 * The next example signature will resolve to {@link EntityContainer#streamEntitiesAsType(Class)}.
 *
 * <pre>
 * <code>
 * {@code @StreamEntities(ofType = false)}
 * {@code Stream<Ghost>} streamGhosts();
 * </code>
 * </pre>
 *
 * The next example signature will show how to use one of the above examples with a subset.
 *
 * <pre>
 * <code>
 * {@code @EntityID(mostSigBits = 0, leastSigBits = 1)}
 * {@code @EntityID(mostSigBits = 0, leastSigBits = 2)}
 * {@code @EntityID(mostSigBits = 0, leastSigBits = 3)}
 * {@code @EntityID(mostSigBits = 0, leastSigBits = 4)}
 * {@code @EntityID(mostSigBits = 0, leastSigBits = 5)}
 * {@code @StreamEntities}
 * {@code Stream<Ghost>} streamGhosts();
 * </code>
 * </pre>
 *
 * NOTE: This function will throw exceptions if {@link StreamEntities} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class StreamEntitiesFunction implements EntityMethodFunction {

    @SuppressWarnings("unchecked")
    @Override
    public StreamEntitiesMethod apply(final Method m) {
	// Check for annotation
	final StreamEntities annonation = m.getAnnotation(StreamEntities.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	if (!hasReturnType(m)) {
	    throw new IllegalArgumentException("Must have a return type");
	} else if (m.getParameterCount() != 0) {
	    throw new IllegalArgumentException("Cannot have any params");
	} else if (m.isDefault()) {
	    throw new IllegalArgumentException("Cannot be default");
	}

	// Check only has one ID max
	final Set<Supplier<UUID>> idSuppliers = new HashSet<>();
	for (final EntityID entityID : m.getAnnotationsByType(EntityID.class)) {
	    idSuppliers.add(toIDSupplier(entityID));
	}

	// Check stream
	if (!returnTypeIs(m, Stream.class)) {
	    throw new IllegalArgumentException("Must have Stream return type");
	}

	// Get entity type
	final Type entityType = firstGenericTypeArg(m.getGenericReturnType());

	// Check entity
	if (!isEntityOrSubtype(toClass(entityType))) {
	    throw new IllegalArgumentException("Entity must be obtainable from return type");
	}

	// Create stream entities method
	return new StreamEntitiesMethod((Class<? extends Entity>) entityType, annonation.ofType(), idSuppliers);
    }
}
