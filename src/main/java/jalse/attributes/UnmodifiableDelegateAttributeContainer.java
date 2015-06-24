package jalse.attributes;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

class UnmodifiableDelegateAttributeContainer implements AttributeContainer {

    private final AttributeContainer delegate;

    UnmodifiableDelegateAttributeContainer(final AttributeContainer delegate) {
	this.delegate = delegate;
    }

    @Override
    public <T> boolean addAttributeListener(final NamedAttributeType<T> namedType, final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void fireAttributeChanged(final NamedAttributeType<T> namedType) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getAttribute(final NamedAttributeType<T> namedType) {
	return delegate != null ? delegate.getAttribute(namedType) : null;
    }

    @Override
    public int getAttributeCount() {
	return delegate != null ? delegate.getAttributeCount() : null;
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final NamedAttributeType<T> namedType) {
	return delegate != null ? delegate.getAttributeListeners(namedType) : Collections.emptySet();
    }

    @Override
    public Set<NamedAttributeType<?>> getAttributeListenerTypes() {
	return delegate != null ? delegate.getAttributeListenerTypes() : Collections.emptySet();
    }

    @Override
    public Set<NamedAttributeType<?>> getAttributeTypes() {
	return delegate != null ? delegate.getAttributeTypes() : Collections.emptySet();
    }

    @Override
    public <T> T removeAttribute(final NamedAttributeType<T> namedType) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean removeAttributeListener(final NamedAttributeType<T> namedType,
	    final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributeListeners() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeAttributeListeners(final NamedAttributeType<T> namedType) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributes() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T setAttribute(final NamedAttributeType<T> namedType, final T attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate != null ? delegate.streamAttributes() : Stream.empty();
    }
}
