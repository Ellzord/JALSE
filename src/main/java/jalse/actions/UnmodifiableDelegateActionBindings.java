package jalse.actions;

import java.util.HashMap;
import java.util.Map;

class UnmodifiableDelegateActionBindings implements ActionBindings {

    private final ActionBindings delegate;

    UnmodifiableDelegateActionBindings(final ActionBindings delegate) {
	this.delegate = delegate;
    }

    @Override
    public <T> T get(final String key) {
	return delegate != null ? delegate.get(key) : null;
    };

    @Override
    public <T> T put(final String key, final T value) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<String, ?> map) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T remove(final String key) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ?> toMap() {
	return delegate != null ? delegate.toMap() : new HashMap<>();
    }
}
