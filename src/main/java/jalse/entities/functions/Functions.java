package jalse.entities.functions;

import jalse.entities.annotations.EntityID;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
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
     * Checks whether the method has params.
     *
     * @param m
     *            Method to check.
     * @throws IllegalArgumentException
     *             If method has no params.
     */
    public static void checkHasParams(final Method m) throws IllegalArgumentException {
	if (!hasParams(m)) {
	    throw new IllegalArgumentException("Must have params");
	}
    }

    /**
     * Checks whether the method has a return type.
     *
     * @param m
     *            Method to check.
     * @throws IllegalArgumentException
     *             If method has no return type.
     */
    public static void checkHasReturnType(final Method m) throws IllegalArgumentException {
	if (!hasReturnType(m)) {
	    throw new IllegalArgumentException("Must have a return type");
	}
    }

    /**
     * Checks whether the method has no params.
     *
     * @param m
     *            Method to check.
     * @throws IllegalArgumentException
     *             If method has params.
     */
    public static void checkNoParams(final Method m) throws IllegalArgumentException {
	if (hasParams(m)) {
	    throw new IllegalArgumentException("Must not have params");
	}
    }

    /**
     * Checks whether the method has a void return type.
     *
     * @param m
     *            Method to check.
     * @throws IllegalArgumentException
     *             If method has a non-void return type.
     */
    public static void checkNoReturnType(final Method m) throws IllegalArgumentException {
	if (hasReturnType(m)) {
	    throw new IllegalArgumentException("Must have a void return type");
	}
    }

    /**
     * Checks whether the method is not default.
     *
     * @param m
     *            Method to check.
     * @throws IllegalArgumentException
     *             If method is default.
     */
    public static void checkNotDefault(final Method m) throws IllegalArgumentException {
	if (m.isDefault()) {
	    throw new IllegalArgumentException("Cannot be default");
	}
    }

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
     * Gets all the {@link EntityID} annotations and transforms into ID suppliers.
     *
     * @param m
     *            Method to check.
     * @return ID supplier set.
     *
     * @see #toIDSupplier(EntityID)
     */
    public static Set<Supplier<UUID>> getIDSuppliers(final Method m) {
	final Set<Supplier<UUID>> idSuppliers = new HashSet<>();
	for (final EntityID entityID : m.getAnnotationsByType(EntityID.class)) {
	    idSuppliers.add(toIDSupplier(entityID));
	}
	return idSuppliers;
    }

    /**
     * Gets a single ID supplier from a method.
     *
     * @param m
     *            Method to check.
     * @return The single ID supplier or {@code null} if none found.
     * @throws IllegalArgumentException
     *             If there are multiple ID suppliers found.
     */
    public static Supplier<UUID> getSingleIDSupplier(final Method m) throws IllegalArgumentException {
	// Check only has one ID max
	final EntityID[] entityIDs = m.getAnnotationsByType(EntityID.class);
	if (entityIDs.length > 1) {
	    throw new IllegalArgumentException("Cannot have more than one entity ID");
	}

	// Get and validate ID
	Supplier<UUID> idSupplier = null;
	if (entityIDs.length == 1) {
	    idSupplier = toIDSupplier(entityIDs[0]);
	}

	return idSupplier;
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
