package jalse.actions;

import static jalse.actions.Actions.requireNotStopped;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * An abstract {@link MutableActionContext} implementation that supplies all of the non-scheduling
 * methods. This is a convenience class for creating an {@link ActionEngine}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public abstract class AbstractActionContext<T> implements MutableActionContext<T> {

    private final ActionEngine engine;
    private final Action<T> action;
    private final MutableActionBindings bindings;
    private T actor;
    private long period;
    private long initialDelay;

    /**
     * Creates a new AbstractActionContext instance with the supplied engine and action.
     *
     * @param engine
     *            Parent engine.
     * @param action
     *            Action this context is for.
     */
    protected AbstractActionContext(final ActionEngine engine, final Action<T> action) {
	this(engine, action, null);
    }

    /**
     * Creates a new AbstractActionContext instance with the supplied engine, action and source
     * bindings.
     *
     * @param engine
     *            Parent engine.
     * @param action
     *            Action this context is for.
     * @param sourceBindings
     *            Bindings to shallow copy.
     */
    protected AbstractActionContext(final ActionEngine engine, final Action<T> action,
	    final ActionBindings sourceBindings) {
	this.engine = requireNotStopped(engine);
	this.action = Objects.requireNonNull(action);
	bindings = new DefaultActionBindings(sourceBindings);
	actor = null;
	period = 0L;
	initialDelay = 0L;
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
    public Optional<T> getActor() {
	return Optional.ofNullable(actor);
    }

    @Override
    public ActionEngine getEngine() {
	return engine;
    }

    @Override
    public long getInitialDelay(final TimeUnit unit) {
	return unit.convert(initialDelay, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getPeriod(final TimeUnit unit) {
	return unit.convert(period, TimeUnit.NANOSECONDS);
    }

    @Override
    public <S> S put(final String key, final S value) {
	return bindings.put(key, value);
    }

    @Override
    public <S> S remove(final String key) {
	return bindings.remove(key);
    }

    @Override
    public void setActor(final T actor) {
	this.actor = actor;
    }

    @Override
    public void setInitialDelay(final long initialDelay, final TimeUnit unit) {
	this.initialDelay = unit.toNanos(initialDelay);
    }

    @Override
    public void setPeriod(final long period, final TimeUnit unit) {
	this.period = unit.toNanos(period);
    }

    @Override
    public Map<String, ?> toMap() {
	return bindings.toMap();
    }
}
