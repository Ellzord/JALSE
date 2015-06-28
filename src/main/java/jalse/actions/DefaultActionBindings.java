package jalse.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is a simple yet fully-featured implementation of {@link MutableActionBindings}.<br>
 * <br>
 * Keys cannot be null or empty.
 *
 * @author Elliot Ford
 *
 */
@SuppressWarnings("unchecked")
public class DefaultActionBindings implements ActionBindings {

    private static void validateKey(final String key) {
	if (key == null) {
	    throw new NullPointerException();
	} else if (key.length() == 0) {
	    throw new IllegalArgumentException();
	}
    }

    private final ConcurrentMap<String, Object> bindings;

    /**
     * Creates a new instance of DefaultActionBindings.
     */
    public DefaultActionBindings() {
	bindings = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new instance of DefaultActionBindings with the supplied source bindings.
     *
     * @param sourceBindings
     *            Source bindings to shallow copy.
     */
    public DefaultActionBindings(final ActionBindings sourceBindings) {
	this(sourceBindings.toMap());
    }

    /**
     * Creates a new instance of DefaultActionBindings with the supplied key-value pairs.
     *
     * @param map
     *            Key-value pairs to add.
     *
     */
    public DefaultActionBindings(final Map<String, ?> map) {
	this();
	putAll(map);
    }

    @Override
    public <T> T get(final String key) {
	validateKey(key);
	return (T) bindings.get(key);
    }

    @Override
    public <T> T put(final String key, final T value) {
	validateKey(key);
	return (T) bindings.put(key, Objects.requireNonNull(value));
    }

    @Override
    public void putAll(final Map<String, ?> map) {
	map.entrySet().forEach(e -> {
	    put(e.getKey(), e.getValue());
	});
    }

    @Override
    public <T> T remove(final String key) {
	validateKey(key);
	return (T) bindings.remove(key);
    }

    @Override
    public void removeAll() {
	bindings.clear();
    }

    @Override
    public Map<String, ?> toMap() {
	return new HashMap<>(bindings);
    }

    @Override
    public String toString() {
	return "DefaultActionBindings [bindings=" + bindings + "]";
    }
}
