package jalse.engine;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an abstract implementation of {@link EngineBindings}. This class provides a
 * {@code protected} scope way to clear the tick bindings ({@link #clearTickBindings()}). This class
 * should be inherited to fully utilise.
 *
 * @author Elliot Ford
 *
 */
@SuppressWarnings("unchecked")
public abstract class AbstractEngineBindings implements EngineBindings {

    private static void validateKey(final String key) {
	if (key == null || key.length() == 0) {
	    throw new IllegalArgumentException();
	}
    }

    private final Map<String, Object> bindings;
    private final Map<String, Object> tickBindings;

    /**
     * Creates a new instance of AbstractEngineBindings with no bound values.
     */
    protected AbstractEngineBindings() {
	this(null);
    }

    /**
     * Creates a new instance of AbstractEngineBindings with the supplied key-value pairs.
     *
     * @param bindings
     *            Key-value pairs to bind.
     */
    protected AbstractEngineBindings(final Map<String, ?> bindings) {
	this.bindings = new ConcurrentHashMap<>();
	if (bindings != null) {
	    this.bindings.putAll(bindings);
	}
	tickBindings = new ConcurrentHashMap<>();
    }

    /**
     * Clicks the tick bindings (this should be done at the end of a tick).
     */
    protected void clearTickBindings() {
	tickBindings.clear();
    }

    @Override
    public <T> T get(final String key) {
	validateKey(key);
	final T value = (T) tickBindings.get(key);
	return value != null ? value : (T) bindings.get(key);
    }

    @Override
    public <T> T put(final String key, final T value) {
	final T oldValue = get(key);
	bindings.put(key, Objects.requireNonNull(value));
	tickBindings.remove(key);
	return oldValue;
    }

    @Override
    public <T> T putForTick(final String key, final T value) {
	final T oldValue = get(key);
	tickBindings.put(key, Objects.requireNonNull(value));
	return oldValue;
    }

    @Override
    public <T> T remove(final String key) {
	final T obj = get(key);
	tickBindings.remove(key);
	bindings.remove(key);
	return obj;
    }
}
