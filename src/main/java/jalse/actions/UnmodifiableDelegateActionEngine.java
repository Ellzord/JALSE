package jalse.actions;

class UnmodifiableDelegateActionEngine implements ActionEngine {

    private final ActionEngine delegate;

    UnmodifiableDelegateActionEngine(final ActionEngine delegate) {
	this.delegate = delegate;
    }

    @Override
    public ActionBindings getBindings() {
	return new UnmodifiableDelegateActionBindings(delegate != null ? delegate.getBindings() : null);
    }

    @Override
    public boolean isPaused() {
	return delegate != null ? delegate.isPaused() : false;
    }

    @Override
    public boolean isStopped() {
	return delegate != null ? delegate.isStopped() : true;
    }

    @Override
    public <T> SchedulableActionContext<T> newContext(final Action<T> action) {
	throw new UnsupportedOperationException();
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
