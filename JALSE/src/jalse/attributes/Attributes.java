package jalse.attributes;

import static jalse.misc.JALSEExceptions.INVALID_ATTRIBUTE_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;

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
     * @param clazz
     *            Class to check.
     * @return The supplied class
     *
     * @throws RuntimeException
     *             If the class does not meet the above requirements.
     */
    public static Class<?> requireNonNullAttrSub(final Class<?> clazz) throws RuntimeException {
	if (clazz == null || Attribute.class.equals(clazz) || !Attribute.class.isAssignableFrom(clazz)) {
	    throwRE(INVALID_ATTRIBUTE_TYPE);
	}
	return clazz;
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
