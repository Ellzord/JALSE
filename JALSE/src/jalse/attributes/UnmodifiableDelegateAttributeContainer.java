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
    public <T> boolean addAttributeListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T addOrNullAttributeOfType(final AttributeType<T> type, final T attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T addOrNullAttributeOfType(final String name, final T attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void fireAttributeChanged(final AttributeType<T> attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void fireAttributeChanged(final String name) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeCount() {
	return delegate != null ? delegate.getAttributeCount() : 0;
    }

    @Override
    public Set<? extends AttributeListener<?>> getAttributeListeners() {
	return delegate != null ? delegate.getAttributeListeners() : Collections.emptySet();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final AttributeType<T> type) {
	return delegate != null ? delegate.getAttributeListeners(type) : Collections.emptySet();
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes() {
	return delegate != null ? delegate.getAttributeListenerTypes() : Collections.emptySet();
    }

    @Override
    public Set<?> getAttributes() {
	return delegate != null ? delegate.getAttributes() : Collections.emptySet();
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes() {
	return delegate != null ? delegate.getAttributeTypes() : Collections.emptySet();
    }

    @Override
    public <T> T getOrNullAttributeOfType(final AttributeType<T> type) {
	return delegate != null ? delegate.getOrNullAttributeOfType(type) : null;
    }

    @Override
    public Object getOrNullAttributeOfType(final String name) {
	return delegate != null ? delegate.getOrNullAttributeOfType(name) : null;
    }

    @Override
    public void removeAllAttributeListeners() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean removeAttributeListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeAttributeListeners(final AttributeType<T> attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributes() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T removeOrNullAttributeOfType(final AttributeType<T> attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object removeOrNullAttributeOfType(final String name) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate != null ? delegate.streamAttributes() : Stream.empty();
    }
}
