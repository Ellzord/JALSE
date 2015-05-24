package jalse.entities.functions;

import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.returnTypeIs;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.KillEntity;
import jalse.entities.methods.KillEntityMethod;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is a method function for {@link KillEntity} annotation. It will resolve an
 * {@link KillEntityMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to {@link EntityContainer#killEntity(UUID)}.
 *
 * <pre>
 * <code>
 * {@code @KillEntity}
 * boolean killGhost(UUID id);
 * 
 * {@code @KillEntity}
 * void killGhost(UUID id);
 * 
 * {@code @EntityID}
 * {@code @KillEntity}
 * boolean killGhost();
 * 
 * {@code @EntityID}
 * {@code @KillEntity}
 * void killGhost();
 * </code>
 * </pre>
 *
 *
 * NOTE: This function will throw exceptions if {@link KillEntity} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class KillEntityFunction implements EntityMethodFunction {

    @Override
    public KillEntityMethod apply(final Method m) {
	// Check for annotation
	final KillEntity annonation = m.getAnnotation(KillEntity.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkNotDefault(m);
	if (m.getParameterCount() > 1) {
	    throw new IllegalArgumentException("Cannot have over one param");
	}

	// Get and validate ID
	final Supplier<UUID> idSupplier = Functions.getSingleIDSupplier(m);

	// Check ID param
	final Class<?>[] params = m.getParameterTypes();
	final boolean idParam = params.length == 1;
	if (idParam && !UUID.class.equals(params[0])) {
	    throw new IllegalArgumentException("Can only have ID parameter");
	}

	// Check duplicate ID definitions
	if (idParam && idSupplier != null) {
	    throw new IllegalArgumentException(String.format("Cannot have %s annotation and ID param", EntityID.class));
	}

	// Check ID
	if (!idParam && idSupplier == null) {
	    throw new IllegalArgumentException("Must provide ID via param or annotation");
	}

	// Work out return type
	if (hasReturnType(m) && !returnTypeIs(m, Boolean.TYPE)) {
	    throw new IllegalArgumentException("Return type must be void or boolean");
	}

	// Create new kill entity method
	final KillEntityMethod kem = new KillEntityMethod();
	if (!idParam) {
	    kem.setIDSupplier(idSupplier);
	}
	return kem;
    }
}
