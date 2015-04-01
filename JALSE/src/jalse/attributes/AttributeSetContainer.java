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
    public <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return delegate.addListener(name, type, listener);
    }

    @Override
    public <T> T addAttributeOfType(final String name, final AttributeType<T> type, final T attr) {
	return delegate.addOfType(name, type, attr);
    }

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	delegate.fireChanged(name, type);
    }

    @Override
    public int getAttributeCount() {
	return delegate.size();
    }

    @Override
    public Set<String> getAttributeListenerNames() {
	return delegate.getListenerNames();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final String name, final AttributeType<T> type) {
	return delegate.getListeners(name, type);
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes(final String name) {
	return delegate.getListenerTypes(name);
    }

    @Override
    public Set<String> getAttributeNames() {
	return delegate.getNames();
    }

    @Override
    public <T> T getAttributeOfType(final String name, final AttributeType<T> type) {
	return delegate.getOfType(name, type);
    }

    @Override
    public Set<?> getAttributes() {
	return Collections.unmodifiableSet(delegate);
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes(final String name) {
	return delegate.getTypes(name);
    }

    @Override
    public <T> boolean removeAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return delegate.removeListener(name, type, listener);
    }

    @Override
    public <T> void removeAttributeListeners(final String name, final AttributeType<T> type) {
	delegate.removeListeners(name, type);
    }

    @Override
    public <T> T removeAttributeOfType(final String name, final AttributeType<T> type) {
	return delegate.removeOfType(name, type);
    }

    @Override
    public void removeAttributes() {
	delegate.clear();
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate.stream();
    }
}
