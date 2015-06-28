package jalse.actions;

import static jalse.actions.Actions.requireNotStopped;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

abstract class BaseActionContext<T> implements SchedulableActionContext<T> {

    private final ActionEngine engine;
    private final Action<T> action;
    private final ActionBindings bindings;
    private final AtomicReference<T> actor;
    private final AtomicLong period;
    private final AtomicLong initialDelay;
    private final AtomicBoolean periodicOnException;

    BaseActionContext(final ActionEngine engine, final Action<T> action, final ActionBindings sourceBindings) {
	this.engine = requireNotStopped(engine);
	this.action = Objects.requireNonNull(action);
	bindings = new DefaultActionBindings(sourceBindings);
	actor = new AtomicReference<>();
	period = new AtomicLong();
	initialDelay = new AtomicLong();
	periodicOnException = new AtomicBoolean();
    }

    @Override
    public <S> S get(final String key) {
	return bindings.get(key);
    }

    @Override
    public Action<T> getAction() {
	return action;
    }

    @Override
    public T getActor() {
	return actor.get();
    }

    @Override
    public ActionEngine getEngine() {
	return engine;
    }

    @Override
    public long getInitialDelay(final TimeUnit unit) {
	return unit.convert(initialDelay.get(), TimeUnit.NANOSECONDS);
    }

    @Override
    public long getPeriod(final TimeUnit unit) {
	return unit.convert(period.get(), TimeUnit.NANOSECONDS);
    }

    @Override
    public boolean isPeriodicOnException() {
	return periodicOnException.get();
    }

    @Override
    public <S> S put(final String key, final S value) {
	return bindings.put(key, value);
    }

    @Override
    public void putAll(final Map<String, ?> map) {
	bindings.putAll(map);
    }

    @Override
    public <S> S remove(final String key) {
	return bindings.remove(key);
    }

    @Override
    public void removeAll() {
	bindings.removeAll();
    }

    @Override
    public void setActor(final T actor) {
	this.actor.set(actor);
    }

    @Override
    public void setInitialDelay(final long initialDelay, final TimeUnit unit) {
	this.initialDelay.set(unit.toNanos(initialDelay));
    }

    @Override
    public void setPeriod(final long period, final TimeUnit unit) {
	this.period.set(unit.toNanos(period));
    }

    @Override
    public void setPeriodicOnException(final boolean periodicOnException) {
	this.periodicOnException.set(periodicOnException);
    }

    @Override
    public Map<String, ?> toMap() {
	return bindings.toMap();
    }
}
