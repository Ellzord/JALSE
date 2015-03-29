package jalse.attributes;

import static jalse.misc.JALSEExceptions.PRIMITIVE_VALUE_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility for {@link AttributeType} value related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Attributes {

    /**
     * Creates an immutable empty attribute container.
     *
     * @return Empty attribute container.
     */
    public static AttributeContainer emptyAttributeContainer() {
	return new UnmodifiableDelegateAttributeContainer(null);
    }

    /**
     * Predicate to check attribute is present.
     *
     * @param type
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is present and {@code false} if it is not.
     */
    public static Predicate<AttributeContainer> isPresent(final AttributeType<?> type) {
	return a -> a.getAttributeOfType(type).isPresent();
    }

    /**
     * Creates a new attribute type (Boolean).
     *
     * @param name
     *            Name of the attribute type.
     * @return New boolean attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Boolean> newBooleanType(final String name) {
	return newType(name, Boolean.class);
    }

    /**
     * Creates a new attribute type (Byte).
     *
     * @param name
     *            Name of the attribute type.
     * @return New byte attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Byte> newByteType(final String name) {
	return newType(name, Byte.class);
    }

    /**
     * Creates a new attribute type (String).
     *
     * @param name
     *            Name of the attribute type.
     * @return New string attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Character> newCharacterType(final String name) {
	return newType(name, Character.class);
    }

    /**
     * Creates a new attribute type (Double).
     *
     * @param name
     *            Name of the attribute type.
     * @return New double attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Double> newDoubleType(final String name) {
	return newType(name, Double.class);
    }

    /**
     * Creates a new attribute type (Object).
     *
     * @param name
     *            Name of attribute type.
     * @return New object attribute type.
     */
    public static AttributeType<?> newEmptyType(final String name) {
	return new AttributeType<>(name, Object.class);
    }

    /**
     * Creates a new attribute type (Float).
     *
     * @param name
     *            Name of the attribute type.
     * @return New float attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Float> newFloatType(final String name) {
	return newType(name, Float.class);
    }

    /**
     * Creates a new attribute type (Integer).
     *
     * @param name
     *            Name of the attribute type.
     * @return New integer attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Integer> newIntegerType(final String name) {
	return newType(name, Integer.class);
    }

    /**
     * Creates a new attribute type (Long).
     *
     * @param name
     *            Name of the attribute type.
     * @return New long attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<Long> newLongType(final String name) {
	return newType(name, Long.class);
    }

    /**
     * Creates a new attribute type (String).
     *
     * @param name
     *            Name of the attribute type.
     * @return New string attribute type.
     *
     * @see #newType(String, Class)
     */
    public static AttributeType<String> newStringType(final String name) {
	return newType(name, String.class);
    }

    /**
     * Creates a new attribute type.
     *
     * @param name
     *            Name of attribute type.
     * @param type
     *            Value type.
     * @return New attribute type.
     */
    public static <T> AttributeType<T> newType(final String name, final Class<T> type) {
	if (type.isPrimitive()) {
	    throwRE(PRIMITIVE_VALUE_TYPE);
	}
	return new AttributeType<>(name, type);
    }

    /**
     * Creates a new type for the specified object (using class).
     *
     * @param name
     *            Attribute type name.
     * @param obj
     *            Object to create attribute type for.
     * @return New attribute type.
     */
    @SuppressWarnings("unchecked")
    public static <T> AttributeType<T> newTypeFor(final String name, final T obj) {
	return (AttributeType<T>) newType(name, obj.getClass());
    }

    /**
     * Predicate to check attribute is not present.
     *
     * @param type
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is not present and {@code false} if it is.
     */
    public static Predicate<AttributeContainer> notPresent(final AttributeType<?> type) {
	return isPresent(type).negate();
    }

    /**
     * Ensures the String is not null or empty.
     *
     * @param str
     *            String to check.
     * @return The string.
     * @throws IllegalArgumentException
     *             If the string was null or empty.
     *
     */
    public static String requireNotEmpty(final String str) throws IllegalArgumentException {
	if (str == null || str.length() == 0) {
	    throw new IllegalArgumentException();
	}
	return str;
    }

    /**
     * Wraps the attribute set as an attribute container.
     *
     * @param attributes
     *            Attribute set.
     * @return Attribute container.
     */
    public static AttributeContainer toAttributeContainer(final AttributeSet attributes) {
	return new AttributeSetContainer(attributes);
    }

    /**
     * Creates an immutable read-only delegate attribute container for the supplied container.
     *
     * @param container
     *            Container to delegate for.
     * @return Immutable attribute container.
     */
    public static AttributeContainer unmodifiableAttributeContainer(final AttributeContainer container) {
	return new UnmodifiableDelegateAttributeContainer(Objects.requireNonNull(container));
    }

    private Attributes() {
	throw new UnsupportedOperationException();
    }
}
