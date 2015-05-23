package jalse.entities.functions;

import static jalse.entities.Entities.isEntityOrSubtype;
import static jalse.entities.functions.Functions.checkHasReturnType;
import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.getIDSuppliers;
import static jalse.entities.functions.Functions.returnTypeIs;
import static jalse.entities.functions.Functions.toClass;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.GetEntities;
import jalse.entities.methods.GetEntitiesMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is a method function for {@link GetEntities} annotation. It will resolve an
 * {@link GetEntitiesMethod} to be used by the entity typing system. This differs from the usual
 * getting of {@link Entity} as it also lets gain a subset of {@link Entity} using {@link EntityID}
 * (so adding to any method will cause it to filter).<br>
 * <br>
 * The next example signature will resolve to {@link EntityContainer#getEntities()}.
 *
 * <pre>
 * <code>
 * {@code @GetEntities}
 * {@code Set<Entity>} getGhosts();
 * </code>
 * </pre>
 *
 * The next example signature will resolve to {@link EntityContainer#getEntitiesOfType(Class)}.
 *
 * <pre>
 * <code>
 * {@code @GetEntities}
 * {@code Set<Ghost>} getGhosts();
 * </code>
 * </pre>
 *
 * The next example signature will resolve to {@link EntityContainer#getEntitiesAsType(Class)}.
 *
 * <pre>
 * <code>
 * {@code @GetEntities(ofType = false)}
 * {@code Set<Ghost>} getGhosts();
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
 * {@code @GetEntities}
 * {@code Stream<Ghost>} getGhosts();
 * </code>
 * </pre>
 *
 * NOTE: This function will throw exceptions if {@link GetEntities} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class GetEntitiesFunction implements EntityMethodFunction {

    @SuppressWarnings("unchecked")
    @Override
    public GetEntitiesMethod apply(final Method m) {
	// Check for annotation
	final GetEntities annonation = m.getAnnotation(GetEntities.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkHasReturnType(m);
	checkNoParams(m);
	checkNotDefault(m);

	// Create ID suppliers
	final Set<Supplier<UUID>> idSuppliers = getIDSuppliers(m);

	// Check stream
	if (!returnTypeIs(m, Set.class)) {
	    throw new IllegalArgumentException("Must have Set return type");
	}

	// Get entity type
	final Type entityType = firstGenericTypeArg(m.getGenericReturnType());

	// Check entity
	if (!isEntityOrSubtype(toClass(entityType))) {
	    throw new IllegalArgumentException("Entity must be obtainable from return type");
	}

	// Create stream entities method
	return new GetEntitiesMethod((Class<? extends Entity>) entityType, idSuppliers, annonation.ofType());
    }
}
