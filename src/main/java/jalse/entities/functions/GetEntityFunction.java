package jalse.entities.functions;

import static jalse.entities.Entities.isEntityOrSubtype;
import static jalse.entities.functions.Functions.checkHasReturnType;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.getSingleIDSupplier;
import static jalse.entities.functions.Functions.returnTypeIs;
import static jalse.entities.functions.Functions.toClass;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.annotations.EntityID;
import jalse.entities.annotations.GetEntity;
import jalse.entities.methods.GetEntityMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This is a method function for {@link GetEntity} annotation. It will resolve an
 * {@link GetEntityMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to {@link EntityContainer#getEntity(UUID)()}.
 *
 * <pre>
 * <code>
 * {@code @GetEntity}
 * Entity getGhost(UUID id);
 * 
 * {@code @EntityID}
 * {@code @GetEntity}
 * Entity getGhost();
 * </code>
 * </pre>
 *
 * The next example signatures will resolve to {@link EntityContainer#getOptEntity(UUID)}.
 *
 * <pre>
 * <code>
 * {@code @GetEntity}
 * {@code Optional<Entity>} getGhost(UUID id);
 * 
 * {@code @EntityID}
 * {@code @GetEntity}
 * {@code Optional<Entity>} getGhost();
 * </code>
 * </pre>
 *
 * The next example signatures will resolve to {@link EntityContainer#getEntityAsType(UUID, Class)}.
 *
 * <pre>
 * <code>
 * {@code @GetEntity}
 * Ghost getGhost(UUID id);
 * 
 * {@code @EntityID}
 * {@code @GetEntity}
 * Ghost getGhost();
 * </code>
 * </pre>
 *
 * The next example signatures will resolve to
 * {@link EntityContainer#getOptEntityAsType(UUID, Class)}.
 *
 * <pre>
 * <code>
 * {@code @GetEntity}
 * {@code Optional<Ghost>} getGhost(UUID id);
 * 
 * {@code @EntityID}
 * {@code @GetEntity}
 * {@code Optional<Ghost>} getGhost();
 * </code>
 * </pre>
 *
 * NOTE: This function will throw exceptions if {@link GetEntity} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class GetEntityFunction implements EntityMethodFunction {

    @SuppressWarnings("unchecked")
    @Override
    public GetEntityMethod apply(final Method m) {
	// Check for annotation
	final GetEntity annonation = m.getAnnotation(GetEntity.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkHasReturnType(m);
	checkNotDefault(m);
	if (m.getParameterCount() > 1) {
	    throw new IllegalArgumentException("Cannot have over one param");
	}

	// Get and validate ID
	Supplier<UUID> idSupplier = getSingleIDSupplier(m);

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

	Type entityType = m.getGenericReturnType();

	// Check is optional
	final boolean optional = returnTypeIs(m, Optional.class);
	if (optional) {
	    entityType = firstGenericTypeArg(entityType);
	}

	// Check entity
	if (!isEntityOrSubtype(toClass(entityType))) {
	    throw new IllegalArgumentException("Entity must be obtainable from return type");
	}

	// Create get entity method
	final GetEntityMethod gem = new GetEntityMethod((Class<? extends Entity>) entityType, optional);
	if (!idParam) {
	    gem.setIDSupplier(idSupplier);
	}
	return gem;
    }
}
