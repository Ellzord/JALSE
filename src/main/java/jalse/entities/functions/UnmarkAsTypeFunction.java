package jalse.entities.functions;

import static jalse.entities.Entities.isEntitySubtype;
import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.returnTypeIs;

import java.lang.reflect.Method;

import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.annotations.UnmarkAsType;
import jalse.entities.methods.UnmarkAsTypeMethod;

/**
 * This is a method function for {@link UnmarkAsType} annotation. It will resolve an
 * {@link UnmarkAsTypeMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to {@link Entity#unmarkAsType(Class)}.
 *
 * <pre>
 * <code>
 * {@code @UnmarkAsType(Ghost.class)}
 * boolean unmarkGhost();
 *
 * {@code @UnmarkAsType(Ghost.class)}
 * void unmarkGhost();
 * </code>
 * </pre>
 *
 *
 * NOTE: This function will throw exceptions if {@link UnmarkAsType} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class UnmarkAsTypeFunction implements EntityMethodFunction {

    @Override
    public UnmarkAsTypeMethod apply(final Method m) {
	// Check for annotation
	final UnmarkAsType annonation = m.getAnnotation(UnmarkAsType.class);
	if (annonation == null) {
	    return null;
	}

	// Check subtype
	final Class<? extends Entity> entityType = annonation.value();
	if (!isEntitySubtype(entityType)) {
	    throw new IllegalArgumentException("Entity type must be a subtype");
	}

	// Basic check method signature
	checkNoParams(m);
	checkNotDefault(m);

	// Check return type.
	if (hasReturnType(m) && !returnTypeIs(m, Boolean.TYPE)) {
	    throw new IllegalArgumentException("Must have void or boolean return type");
	}

	// Create get entity method
	return new UnmarkAsTypeMethod(entityType);
    }
}
