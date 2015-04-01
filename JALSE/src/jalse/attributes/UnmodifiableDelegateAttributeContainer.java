package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

class UnmodifiableDelegateAttributeContainer implements AttributeContainer {

    private final AttributeContainer delegate;

    UnmodifiableDelegateAttributeContainer(final AttributeContainer delegate) {
	this.delegate = delegate;
    }

    @Override
    public <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T addAttributeOfType(final String name, final AttributeType<T> type, final T attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeCount() {
	return delegate != null ? delegate.getAttributeCount() : 0;
    }

    @Override
    public Set<String> getAttributeListenerNames() {
	return delegate != null ? delegate.getAttributeListenerNames() : Collections.emptySet();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final String name, final AttributeType<T> type) {
	return delegate != null ? delegate.getAttributeListeners(name, type) : Collections.emptySet();
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes(final String name) {
	return delegate != null ? delegate.getAttributeListenerTypes(name) : Collections.emptySet();
    }

    @Override
    public Set<String> getAttributeNames() {
	return delegate != null ? delegate.getAttributeNames() : Collections.emptySet();
    }

    @Override
    public <T> T getAttributeOfType(final String name, final AttributeType<T> type) {
	return delegate != null ? delegate.getAttributeOfType(name, type) : null;
    }

    @Override
    public Set<?> getAttributes() {
	return delegate != null ? delegate.getAttributes() : Collections.emptySet();
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes(final String name) {
	return delegate != null ? delegate.getAttributeTypes(name) : Collections.emptySet();
    }

    @Override
    public <T> boolean removeAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeAttributeListeners(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T removeAttributeOfType(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributes() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate != null ? delegate.streamAttributes() : Stream.empty();
    }
}
