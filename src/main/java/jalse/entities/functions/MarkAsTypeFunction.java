package jalse.entities.functions;

import static jalse.entities.Entities.isEntitySubtype;
import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.returnTypeIs;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.annotations.MarkAsType;
import jalse.entities.methods.MarkAsTypeMethod;

import java.lang.reflect.Method;

/**
 * This is a method function for {@link MarkAsType} annotation. It will resolve an
 * {@link MarkAsTypeMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to {@link Entity#MarkAsType(Class)()}.
 *
 * <pre>
 * <code>
 * {@code @MarkAsType(Ghost.class)}
 * boolean markGhost();
 * 
 * {@code @MarkAsType(Ghost.class)}
 * void markGhost();
 * </code>
 * </pre>
 *
 *
 * NOTE: This function will throw exceptions if {@link MarkAsType} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class MarkAsTypeFunction implements EntityMethodFunction {

    @Override
    public MarkAsTypeMethod apply(final Method m) {
	// Check for annotation
	final MarkAsType annonation = m.getAnnotation(MarkAsType.class);
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
	return new MarkAsTypeMethod(entityType);
    }
}
