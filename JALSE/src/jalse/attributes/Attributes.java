package jalse.attributes;

import static jalse.misc.JALSEExceptions.INVALID_ATTRIBUTE_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.listeners.AttributeListener;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A utility for {@link Attribute} related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Attributes {

    private static class UnmodifiableDelegateAttributeContainer implements AttributeContainer {

	private final AttributeContainer delegate;

	private UnmodifiableDelegateAttributeContainer(final AttributeContainer delegate) {

	    this.delegate = delegate;
	}

	@Override
	public boolean addAttributeListener(final AttributeListener<? extends Attribute> listener) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Attribute> Optional<T> addAttributeOfType(final T attr) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Attribute> boolean fireAttributeChanged(final Class<T> attr) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Attribute> Set<? extends AttributeListener<T>> getAttributeListeners(final Class<T> attr) {

	    return delegate != null ? delegate.getAttributeListeners(attr) : Collections.emptySet();
	}

	@Override
	public Set<? extends AttributeListener<? extends Attribute>> getAttributeListeners() {

	    return delegate != null ? delegate.getAttributeListeners() : Collections.emptySet();
	}

	@Override
	public Set<Class<? extends Attribute>> getAttributeListenerTypes() {

	    return delegate != null ? delegate.getAttributeListenerTypes() : Collections.emptySet();
	}

	@Override
	public <T extends Attribute> Optional<T> getAttributeOfType(final Class<T> attr) {

	    return delegate != null ? delegate.getAttributeOfType(attr) : Optional.empty();
	}

	@Override
	public boolean removeAttributeListener(final AttributeListener<? extends Attribute> listener) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends Attribute> getAttributes() {

	    return delegate != null ? delegate.getAttributes() : Collections.emptySet();
	}

	@Override
	public int getAttributeCount() {

	    return delegate != null ? delegate.getAttributeCount() : 0;
	}

	@Override
	public void removeAttributes() {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Set<Class<? extends Attribute>> getAttributeTypes() {

	    return delegate != null ? delegate.getAttributeTypes() : Collections.emptySet();
	}

	@Override
	public <T extends Attribute> Optional<T> removeAttributeOfType(final Class<T> attr) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Stream<? extends Attribute> streamAttributes() {

	    return delegate != null ? delegate.streamAttributes() : Stream.empty();
	}
    }

    /**
     * Creates an immutable empty attribute container.
     *
     * @return Empty attribute container.
     */
    public static AttributeContainer emptyAttributeContainer() {

	return new UnmodifiableDelegateAttributeContainer(null);
    }

    /**
     * Creates an immutable read-only delegate attribute container for the
     * supplied container.
     *
     * @param container
     *            Container to delegate for.
     * @return Immutable attribute container.
     */
    public static AttributeContainer unmodifiableAttributeContainer(final AttributeContainer container) {

	return new UnmodifiableDelegateAttributeContainer(Objects.requireNonNull(container));
    }

    /**
     * Validation method for ensuring the class is a descendant of Attribute
     * (but also not Attribute itself).
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
