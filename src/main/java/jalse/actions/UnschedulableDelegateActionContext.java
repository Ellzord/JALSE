package jalse.actions;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class UnschedulableDelegateActionContext<T> implements SchedulableActionContext<T> {

    private final SchedulableActionContext<T> delegate;

    UnschedulableDelegateActionContext(final SchedulableActionContext<T> delegate) {
	this.delegate = delegate;
    }

    @Override
    public void await() throws InterruptedException {
	if (delegate == null) {
	    throw new UnsupportedOperationException();
	}
	delegate.await();
    }

    @Override
    public boolean cancel() {
	return delegate != null ? delegate.cancel() : false;
    }

    @Override
    public <S> S get(final String key) {
	return delegate != null ? delegate.get(key) : null;
    }

    @Override
    public Action<T> getAction() {
	throw new UnsupportedOperationException();
    }

    @Override
    public T getActor() {
	return delegate != null ? delegate.getActor() : null;
    }

    @Override
    public ActionEngine getEngine() {
	if (delegate == null) {
	    return Actions.emptyActionEngine();
	}
	return delegate.getEngine();
    }

    @Override
    public long getInitialDelay(final TimeUnit unit) {
	return delegate != null ? delegate.getInitialDelay(unit) : 0L;
    }

    @Override
    public long getPeriod(final TimeUnit unit) {
	return delegate != null ? delegate.getPeriod(unit) : 0L;
    }

    @Override
    public boolean isCancelled() {
	return delegate != null ? delegate.isCancelled() : false;
    }

    @Override
    public boolean isDone() {
	return delegate != null ? delegate.isDone() : true;
    }

    @Override
    public boolean isPeriodicOnException() {
	return delegate != null ? delegate.isPeriodicOnException() : false;
    }

    @Override
    public <S> S put(final String key, final S value) {
	if (delegate == null) {
	    throw new UnsupportedOperationException();
	}
	return delegate.put(key, value);
    }

    @Override
    public void putAll(final Map<String, ?> map) {
	if (delegate == null) {
	    throw new UnsupportedOperationException();
	}
	delegate.putAll(map);
    }

    @Override
    public <S> S remove(final String key) {
	if (delegate == null) {
	    throw new UnsupportedOperationException();
	}
	return delegate.remove(key);
    }

    @Override
    public void removeAll() {
	if (delegate == null) {
	    throw new UnsupportedOperationException();
	}
	delegate.removeAll();
    }

    @Override
    public void schedule() {
	throw new UnsupportedOperationException();
    }

    @Override
    public void scheduleAndAwait() throws InterruptedException {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setActor(final T actor) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setInitialDelay(final long initialDelay, final TimeUnit unit) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setPeriod(final long period, final TimeUnit unit) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setPeriodicOnException(final boolean periodicOnException) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ?> toMap() {
	return delegate != null ? delegate.toMap() : Collections.emptyMap();
    }
}
