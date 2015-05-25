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
    public <T> boolean addAttributeContainerListener(final String name, final AttributeType<T> type,
	    final AttributeContainerListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getAttribute(final String name, final AttributeType<T> type) {
	return delegate != null ? delegate.getAttribute(name, type) : null;
    }

    @Override
    public Set<String> getAttributeContainerListenerNames() {
	return delegate != null ? delegate.getAttributeContainerListenerNames() : Collections.emptySet();
    }

    @Override
    public <T> Set<? extends AttributeContainerListener<T>> getAttributeContainerListeners(final String name,
	    final AttributeType<T> type) {
	return delegate != null ? delegate.getAttributeContainerListeners(name, type) : Collections.emptySet();
    }

    @Override
    public Set<AttributeType<?>> getAttributeContainerListenerTypes(final String name) {
	return delegate != null ? delegate.getAttributeContainerListenerTypes(name) : Collections.emptySet();
    }

    @Override
    public int getAttributeCount() {
	return delegate != null ? delegate.getAttributeCount() : 0;
    }

    @Override
    public Set<String> getAttributeNames() {
	return delegate != null ? delegate.getAttributeNames() : Collections.emptySet();
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
    public <T> T removeAttribute(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean removeAttributeContainerListener(final String name, final AttributeType<T> type,
	    final AttributeContainerListener<T> listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributeContainerListeners() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeAttributeContainerListeners(final String name, final AttributeType<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttributes() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T setAttribute(final String name, final AttributeType<T> type, final T attr) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<?> streamAttributes() {
	return delegate != null ? delegate.streamAttributes() : Stream.empty();
    }
}
