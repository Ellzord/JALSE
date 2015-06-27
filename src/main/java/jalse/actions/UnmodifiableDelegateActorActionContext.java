package jalse.actions;

import java.util.Map;
import java.util.concurrent.TimeUnit;

class UnmodifiableActorDelegateActionContext<T> implements MutableActionContext<T> {

    private final MutableActionContext<T> delegate;

    UnmodifiableActorDelegateActionContext(final MutableActionContext<T> delegate) {
	this.delegate = delegate;
    }

    @Override
    public void await() throws InterruptedException {
	delegate.await();
    }

    @Override
    public boolean cancel() {
	return delegate.cancel();
    }

    @Override
    public <S> S get(final String key) {
	return delegate.get(key);
    }

    @Override
    public Action<T> getAction() {
	return delegate.getAction();
    }

    @Override
    public T getActor() {
	return delegate.getActor();
    }

    @Override
    public ActionEngine getEngine() {
	return delegate.getEngine();
    }

    @Override
    public long getInitialDelay(final TimeUnit unit) {
	return delegate.getInitialDelay(unit);
    }

    @Override
    public long getPeriod(final TimeUnit unit) {
	return delegate.getPeriod(unit);
    }

    @Override
    public boolean isCancelled() {
	return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
	return delegate.isDone();
    }

    @Override
    public boolean isPeriodicOnException() {
	return delegate.isPeriodicOnException();
    }

    @Override
    public <S> S put(final String key, final S value) {
	return delegate.put(key, value);
    }

    @Override
    public void putAll(final Map<String, ?> map) {
	delegate.putAll(map);
    }

    @Override
    public <S> S remove(final String key) {
	return delegate.remove(key);
    }

    @Override
    public void removeAll() {
	delegate.removeAll();
    }

    @Override
    public void schedule() {
	delegate.schedule();
    }

    @Override
    public void setActor(final T actor) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setInitialDelay(final long initialDelay, final TimeUnit unit) {
	delegate.setInitialDelay(initialDelay, unit);
    }

    @Override
    public void setPeriod(final long period, final TimeUnit unit) {
	delegate.setPeriod(period, unit);
    }

    @Override
    public void setPeriodicOnException(final boolean periodicOnException) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ?> toMap() {
	return delegate.toMap();
    }
}