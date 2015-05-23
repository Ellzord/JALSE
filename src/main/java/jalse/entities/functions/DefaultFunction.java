package jalse.entities.functions;

import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.methods.DefaultMethod;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * This is a method function for {@code default} methods. It will resolve an {@link DefaultMethod}
 * to be used by the entity typing system.<br>
 * <br>
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class DefaultFunction implements EntityMethodFunction {

    private static final Constructor<Lookup> LOOKUP_CONSTRUCTOR;

    static {
	try {
	    LOOKUP_CONSTRUCTOR = Lookup.class.getDeclaredConstructor(Class.class, int.class);
	    LOOKUP_CONSTRUCTOR.setAccessible(true);
	} catch (final Exception e) {
	    throw new Error("Could not get private Lookup constructor instance", e);
	}
    }

    @Override
    public DefaultMethod apply(final Method m) {
	// Check default
	if (!m.isDefault()) {
	    return null;
	}

	// Create lookup
	Lookup lookup;
	try {
	    lookup = LOOKUP_CONSTRUCTOR.newInstance(m.getDeclaringClass(), Lookup.PRIVATE);
	} catch (ReflectiveOperationException | IllegalArgumentException e) {
	    throw new RuntimeException(e);
	}

	// Create default method
	return new DefaultMethod(m, lookup);
    }
}
