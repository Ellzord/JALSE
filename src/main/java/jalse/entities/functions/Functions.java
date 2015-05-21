package jalse.entities.functions;

import jalse.entities.annotations.EntityID;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A utility for creating {@link EntityFunction} implementations.
 *
 * @author Elliot Ford
 *
 */
public final class Functions {

    /**
     * An supplier for random IDs.
     *
     * @see UUID#randomUUID()
     */
    public static final Supplier<UUID> RANDOM_ID_SUPPLIER = UUID::randomUUID;

    /**
     * Gets the first generic type argument for a parameterised type.
     *
     * @param pt
     *            Type to take from.
     * @return The first generic type argument or null if not a parameterised type.
     */
    public static Type firstGenericTypeArg(final Type pt) {
	return pt instanceof ParameterizedType ? ((ParameterizedType) pt).getActualTypeArguments()[0] : null;
    }

    /**
     * Wether the method has parameters.
     *
     * @param m
     *            Method to check.
     * @return Whether it had parameters.
     */
    public static boolean hasParams(final Method m) {
	return m.getParameterCount() > 0;
    }

    /**
     * Checks whether a method has a return type.
     *
     * @param m
     *            Method to check.
     * @return Whether it had a return type.
     */
    public static boolean hasReturnType(final Method m) {
	return !Void.TYPE.equals(m.getGenericReturnType());
    }

    /**
     * Whether the type represents a primitive type.
     *
     * @param t
     *            Type to check.
     * @return Whether the type is primitive.
     */
    public static boolean isPrimitive(final Type t) {
	return t instanceof Class<?> && ((Class<?>) t).isPrimitive();
    }

    /**
     * Whether the method return type matches the supplied type.
     *
     * @param m
     *            Method to check.
     * @param clazz
     *            Possible return type to check.
     * @return Whether the actual return type matches the check type.
     */
    public static boolean returnTypeIs(final Method m, final Class<?> clazz) {
	return clazz.equals(toClass(m.getGenericReturnType()));
    }

    /**
     * Attempts to resolve raw class from type.
     *
     * @param type
     *            Type to resolve for.
     * @return Resolved class.
     */
    public static Class<?> toClass(final Type type) {
	return type instanceof ParameterizedType ? toClass(((ParameterizedType) type).getRawType()) : (Class<?>) type;
    }

    /**
     * Transforms the {@link EntityID} to a {@link UUID} supplier.
     *
     * @param id
     *            ID annotation.
     * @return ID supplier using annotation.
     *
     * @see #validateEntityID(EntityID)
     */
    public static Supplier<UUID> toIDSupplier(final EntityID id) {
	validateEntityID(id);
	// Random supplier
	if (id.random()) {
	    return RANDOM_ID_SUPPLIER;
	}
	// Create ID
	final UUID newID;
	if (id.name().length() > 0) {
	    newID = UUID.fromString(id.name());
	} else {
	    newID = new UUID(id.mostSigBits(), id.leastSigBits());
	}
	// Create static ID supplier.
	return () -> newID;
    }

    /**
     * Validates an {@link EntityID} annotation is correctly formed.
     *
     * @param id
     *            ID to check.
     */
    public static void validateEntityID(final EntityID id) {
	int changes = 0;
	// Changed new UUID(m, l)
	if (id.mostSigBits() != EntityID.DEFAULT_MOST_SIG_BITS || id.leastSigBits() != EntityID.DEFAULT_LEAST_SIG_BITS) {
	    changes++;
	}

	// Changed fromString(n)
	if (!EntityID.DEFAULT_NAME.equals(id.name())) {
	    changes++;
	}

	// Changed random
	if (id.random() != EntityID.DEFAULT_RANDOM) {
	    changes++;
	}

	// Check changed more than once
	if (changes > 1) {
	    throw new IllegalArgumentException(String.format("%s annotation provides multiple ID source info",
		    EntityID.class));
	}
    }

    private Functions() {
	throw new UnsupportedOperationException();
    }
}
