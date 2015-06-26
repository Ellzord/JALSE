package jalse.attributes;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A utility for {@link AttributeType} value related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Attributes {

    /**
     * {@link Boolean} Attribute type.
     */
    public static final AttributeType<Boolean> BOOLEAN_TYPE = new AttributeType<Boolean>() {};

    /**
     * {@link Integer} Attribute type.
     */
    public static final AttributeType<Integer> INTEGER_TYPE = new AttributeType<Integer>() {};

    /**
     * {@link String} Attribute type.
     */
    public static final AttributeType<String> STRING_TYPE = new AttributeType<String>() {};

    /**
     * {@link Double} Attribute type.
     */
    public static final AttributeType<Double> DOUBLE_TYPE = new AttributeType<Double>() {};

    /**
     * {@link Character} Attribute type.
     */
    public static final AttributeType<Character> CHARACTER_TYPE = new AttributeType<Character>() {};

    /**
     * {@link Long} Attribute type.
     */
    public static final AttributeType<Long> LONG_TYPE = new AttributeType<Long>() {};

    /**
     * {@link Byte} Attribute type.
     */
    public static final AttributeType<Byte> BYTE_TYPE = new AttributeType<Byte>() {};

    /**
     * {@link Float} Attribute type.
     */
    public static final AttributeType<Float> FLOAT_TYPE = new AttributeType<Float>() {};

    /**
     * {@link Short} Attribute type.
     */
    public static final AttributeType<Short> SHORT_TYPE = new AttributeType<Short>() {};

    /**
     * {@link Object} Attribute type.
     */
    public static final AttributeType<Object> OBJECT_TYPE = new AttributeType<Object>() {};

    /**
     * An empty AttributeContainer.
     */
    public static final AttributeContainer EMPTY_ATTRIBUTECONTAINER = new UnmodifiableDelegateAttributeContainer(null);

    /**
     * Creates an immutable empty attribute container.
     *
     * @return Empty attribute container.
     */
    public static AttributeContainer emptyAttributeContainer() {
	return EMPTY_ATTRIBUTECONTAINER;
    }

    /**
     * Creates a new attribute type (Boolean).
     *
     * @param name
     *            Name of the attribute type.
     * @return New boolean attribute type.
     *
     * @see #BOOLEAN_TYPE
     */
    public static NamedAttributeType<Boolean> newNamedBooleanType(final String name) {
	return new NamedAttributeType<>(name, BOOLEAN_TYPE);
    }

    /**
     * Creates a new attribute type (Byte).
     *
     * @param name
     *            Name of the attribute type.
     * @return New byte attribute type.
     *
     * @see #BYTE_TYPE
     */
    public static NamedAttributeType<Byte> newNamedByteType(final String name) {
	return new NamedAttributeType<>(name, BYTE_TYPE);
    }

    /**
     * Creates a new attribute type (String).
     *
     * @param name
     *            Name of the attribute type.
     * @return New string attribute type.
     *
     * @see #CHARACTER_TYPE
     */
    public static NamedAttributeType<Character> newNamedCharacterType(final String name) {
	return new NamedAttributeType<>(name, CHARACTER_TYPE);
    }

    /**
     * Creates a new attribute type (Double).
     *
     * @param name
     *            Name of the attribute type.
     * @return New double attribute type.
     *
     * @see #DOUBLE_TYPE
     */
    public static NamedAttributeType<Double> newNamedDoubleType(final String name) {
	return new NamedAttributeType<>(name, DOUBLE_TYPE);
    }

    /**
     * Creates a new attribute type (Float).
     *
     * @param name
     *            Name of the attribute type.
     * @return New float attribute type.
     *
     * @see #FLOAT_TYPE
     */
    public static NamedAttributeType<Float> newNamedFloatType(final String name) {
	return new NamedAttributeType<>(name, FLOAT_TYPE);
    }

    /**
     * Creates a new attribute type (Integer).
     *
     * @param name
     *            Name of the attribute type.
     * @return New integer attribute type.
     *
     * @see #INTEGER_TYPE
     */
    public static NamedAttributeType<Integer> newNamedIntegerType(final String name) {
	return new NamedAttributeType<>(name, INTEGER_TYPE);
    }

    /**
     * Creates a new attribute type (Long).
     *
     * @param name
     *            Name of the attribute type.
     * @return New long attribute type.
     *
     * @see #LONG_TYPE
     */
    public static NamedAttributeType<Long> newNamedLongType(final String name) {
	return new NamedAttributeType<>(name, LONG_TYPE);
    }

    /**
     * Creates a new attribute type (Object).
     *
     * @param name
     *            Name of attribute type.
     * @return New object attribute type.
     *
     * @see #OBJECT_TYPE
     */
    public static NamedAttributeType<Object> newNamedObjectType(final String name) {
	return new NamedAttributeType<>(name, OBJECT_TYPE);
    }

    /**
     * Creates a new attribute type (Short).
     *
     * @param name
     *            Name of the attribute type.
     * @return New short attribute type.
     *
     * @see #SHORT_TYPE
     */
    public static NamedAttributeType<Short> newNamedShortType(final String name) {
	return new NamedAttributeType<>(name, SHORT_TYPE);
    }

    /**
     * Creates a new attribute type (String).
     *
     * @param name
     *            Name of the attribute type.
     * @return New string attribute type.
     *
     * @see #STRING_TYPE
     */
    public static NamedAttributeType<String> newNamedStringType(final String name) {
	return new NamedAttributeType<>(name, STRING_TYPE);
    }

    /**
     * Creates a new named type of the supplied simple type.
     *
     * @param name
     *            Name of the attribute type.
     * @param type
     *            Simple type.
     * @return New named attribute type.
     *
     * @see #newTypeOf(Class)
     */
    public static <T> NamedAttributeType<T> newNamedTypeOf(final String name, final Class<T> type) {
	return new NamedAttributeType<>(name, newTypeOf(type));
    }

    /**
     * Creates a new named unknown type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Unknown type.
     * @return New named unknown type.
     */
    public static NamedAttributeType<Object> newNamedUnknownType(final String name, final Type type) {
	return new NamedAttributeType<>(name, new AttributeType<Object>(type) {});
    }

    /**
     * Creates a new attribute type of the supplied simple type.
     *
     * @param type
     *            Simple type.
     * @return Newly created simple type.
     *
     * @see AttributeType
     */
    public static <T> AttributeType<T> newTypeOf(final Class<T> type) {
	return new AttributeType<T>(type) {};
    }

    /**
     * Creates a new unknown attribute type from the supplied type.
     *
     * @param type
     *            Unknown type.
     * @return Newly created unknown type.
     */
    public static AttributeType<Object> newUnknownType(final Type type) {
	return new AttributeType<Object>(type) {};
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
    public static String requireNotEmpty(final String str) throws NullPointerException, IllegalArgumentException {
	if (str.length() == 0) {
	    throw new IllegalArgumentException();
	}
	return str;
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
