package jalse.actions;

class UnmodifiableDelegateActionEngine implements ActionEngine {

    private final ActionEngine delegate;
    private final UnmodifiableDelegateActionBindings bindings;

    UnmodifiableDelegateActionEngine(final ActionEngine delegate) {
	this.delegate = delegate;
	bindings = new UnmodifiableDelegateActionBindings(delegate != null ? delegate.getBindings() : null);
    }

    @Override
    public MutableActionBindings getBindings() {
	return bindings;
    }

    @Override
    public boolean isPaused() {
	return delegate != null ? delegate.isPaused() : false;
    }

    @Override
    public boolean isStopped() {
	return delegate != null ? delegate.isPaused() : true;
    }

    @Override
    public <T> MutableActionContext<T> newContext(final Action<T> action) {
	return new UnmodifiableDelegateActionContext<>(delegate != null ? delegate.newContext(action) : null);
    }

    @Override
    public void pause() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void stop() {
	throw new UnsupportedOperationException();
    }
}
