package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.functions.DefaultFunction;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * This is used for mapping {@code default} methods.
 *
 * @author Elliot Ford
 *
 * @see DefaultFunction
 */
public class DefaultMethod implements EntityMethod {

    private final Method m;
    private final Lookup lookup;

    /**
     * Creates a new default method.
     *
     * @param m
     *            Method to invoke.
     * @param lookup
     *            Method class lookup.
     */
    public DefaultMethod(final Method m, final Lookup lookup) {
	this.m = Objects.requireNonNull(m);
	this.lookup = Objects.requireNonNull(lookup);
    }

    /**
     * Gets the method to invoke.
     *
     * @return Type method.
     */
    public Method getMethod() {
	return m;
    }

    @Override
    public Object invoke(final Object proxy, final Entity e, final Object[] args) throws Throwable {
	return lookup.unreflectSpecial(m, m.getDeclaringClass()).bindTo(proxy).invokeWithArguments(args);
    }
}
