package jalse.entities.functions;

import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNoReturnType;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.getIDSuppliers;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.KillEntities;
import jalse.entities.methods.KillEntitiesMethod;

/**
 * This is a method function for {@link KillEntities} annotation. It will resolve an
 * {@link KillEntitiesMethod} to be used by the entity typing system. This differs from the usual
 * killing of {@link Entity} as it also lets kill a subset of {@link Entity} using {@link EntityID}
 * (so adding to any method will cause it to filter).<br>
 * <br>
 * The next example signature will resolve to {@link EntityContainer#killEntities()}.
 *
 * <pre>
 * <code>
 * {@code @KillEntities}
 * void killGhosts();
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
 * {@code @KillEntities}
 * void killGhosts();
 * </code>
 * </pre>
 *
 * NOTE: This function will throw exceptions if {@link KillEntities} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class KillEntitiesFunction implements EntityMethodFunction {

    @Override
    public KillEntitiesMethod apply(final Method m) {
	// Check for annotation
	final KillEntities annonation = m.getAnnotation(KillEntities.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkNoReturnType(m);
	checkNoParams(m);
	checkNotDefault(m);

	// Create ID suppliers
	final Set<Supplier<UUID>> idSuppliers = getIDSuppliers(m);

	// Create stream entities method
	return new KillEntitiesMethod(idSuppliers);
    }
}
