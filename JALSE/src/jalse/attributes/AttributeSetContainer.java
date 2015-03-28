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
    public boolean addAttributeListener(final AttributeListener<? extends Attribute> listener) {
	return delegate.addListener(listener);
    }

    @Override
    public <T extends Attribute> T addOrNullAttributeOfType(final T attr) {
	return delegate.addOfType(attr);
    }

    @Override
    public <T extends Attribute> void fireAttributeChanged(final Class<T> attr) {
	delegate.fireChanged(attr);
    }

    @Override
    public int getAttributeCount() {
	return delegate.size();
    }

    @Override
    public Set<? extends AttributeListener<? extends Attribute>> getAttributeListeners() {
	return delegate.getListeners();
    }

    @Override
    public <T extends Attribute> Set<? extends AttributeListener<T>> getAttributeListeners(final Class<T> attr) {
	return delegate.getListeners(attr);
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeListenerTypes() {
	return delegate.getListenerTypes();
    }

    @Override
    public Set<? extends Attribute> getAttributes() {
	return Collections.unmodifiableSet(delegate);
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeTypes() {
	return delegate.getAttributeTypes();
    }

    @Override
    public <T extends Attribute> T getOrNullAttributeOfType(final Class<T> attr) {
	return delegate.getOfType(attr);
    }

    @Override
    public void removeAllAttributeListeners() {
	delegate.removeAllListeners();
    }

    @Override
    public boolean removeAttributeListener(final AttributeListener<? extends Attribute> listener) {
	return delegate.removeListener(listener);
    }

    @Override
    public <T extends Attribute> void removeAttributeListeners(final Class<T> attr) {
	delegate.removeListeners(attr);
    }

    @Override
    public void removeAttributes() {
	delegate.clear();
    }

    @Override
    public <T extends Attribute> T removeOrNullAttributeOfType(final Class<T> attr) {
	return delegate.removeOfType(attr);
    }

    @Override
    public Stream<? extends Attribute> streamAttributes() {
	return delegate.stream();
    }
}
