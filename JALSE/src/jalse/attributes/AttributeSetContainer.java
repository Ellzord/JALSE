package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

class AttributeSetContainer implements AttributeContainer {

    private final AttributeSet delegate;

    AttributeSetContainer(final AttributeSet delegate) {
	this.delegate = delegate;
    }

    @Override
    public <T> boolean addAttributeListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	return delegate.addListener(type, listener);
    }

    @Override
    public <T> T addOrNullAttributeOfType(final AttributeType<T> type, final T attr) {
	return delegate.addOfType(type, attr);
    }

    @Override
    public <T> void fireAttributeChanged(final AttributeType<T> type) {
	delegate.fireChanged(type);
    }

    @Override
    public int getAttributeCount() {
	return delegate.size();
    }

    @Override
    public Set<? extends AttributeListener<?>> getAttributeListeners() {
	return delegate.getListeners();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final AttributeType<T> type) {
	return delegate.getListeners(type);
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes() {
	return delegate.getListenerTypes();
    }

    @Override
    public Set<?> getAttributes() {
	return Collections.unmodifiableSet(delegate);
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes() {
	return delegate.getAttributeTypes();
    }

    @Override
    public <T> T getOrNullAttributeOfType(final AttributeType<T> attr) {
	return delegate.getOfType(attr);
    }

    @Override
    public void removeAllAttributeListeners() {
	delegate.removeAllListeners();
    }

    @Override
    public <T> boolean removeAttributeListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	return delegate.removeListener(type, listener);
    }

    @Override
    public <T> void removeAttributeListeners(final AttributeType<T> type) {
	delegate.removeListeners(type);
    }

    @Override
    public void removeAttributes() {
	delegate.clear();
    }

    @Override
    public <T> T removeOrNullAttributeOfType(final AttributeType<T> type) {
	return delegate.removeOfType(type);
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate.stream();
    }
}
