package jalse.attributes;

import static jalse.misc.JALSEExceptions.INVALID_ATTRIBUTE_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import static jalse.misc.TypeParameterResolver.toClass;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A utility for {@link Attribute} related functionality.
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
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is present and {@code false} if it is not.
     */
    public static Predicate<AttributeContainer> isPresent(final Class<? extends Attribute> attr) {
	return a -> a.getAttributeOfType(attr).isPresent();
    }

    /**
     * Predicate to check attribute is not present.
     *
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is not present and {@code false} if it is.
     */
    public static Predicate<AttributeContainer> notPresent(final Class<? extends Attribute> attr) {
	return isPresent(attr).negate();
    }

    /**
     * Validation method for ensuring the class is a descendant of Attribute (but also not Attribute
     * itself).
     *
     * @param attr
     *            Class to check.
     * @return The supplied class
     *
     * @throws RuntimeException
     *             If the class does not meet the above requirements.
     */
    public static <T extends Attribute> Class<T> requireNonNullAttrSub(final Class<T> attr) throws RuntimeException {
	return requireNonNullNonAttribute(attr);
    }

    /**
     * Validation method for ensuring the class is a descendant of Attribute (but also not Attribute
     * itself).
     *
     * @param clazz
     *            Class to check.
     * @return The supplied class
     *
     * @throws RuntimeException
     *             If the class does not meet the above requirements.
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Attribute> requireNonNullAttrSub(final Type clazz) throws RuntimeException {
	final Class<?> attr = requireNonNullNonAttribute(toClass(clazz));
	if (!Attribute.class.isAssignableFrom(attr)) {
	    throwRE(INVALID_ATTRIBUTE_TYPE);
	}

	return (Class<? extends Attribute>) attr;
    }

    private static <T> Class<T> requireNonNullNonAttribute(final Class<T> clazz) {
	if (clazz == null || Attribute.class.equals(clazz)) {
	    throwRE(INVALID_ATTRIBUTE_TYPE);
	}

	return clazz;
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

    /**
     * Convenience method for getting the value within a optional wrapper.
     *
     * @param attr
     *            Optional non-attribute wrapper.
     * @return The unwrapped object or null if not present.
     */
    public static <T> T unwrap(final Optional<? extends NonAttributeWrapper<T>> attr) {
	return attr.isPresent() ? attr.get().unwrap() : null;
    }

    private Attributes() {
	throw new UnsupportedOperationException();
    }
}
