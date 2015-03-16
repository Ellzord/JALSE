package jalse.engine;

/**
 * A key-value bindings for {@link Engine} (keys are strings). Values can be bound forever or for
 * only the remaining duration of the tick ({@link #putForTick(String, Object)}). The
 * {@link #get(String)} operation will retrieve the current tick bound value over the existing
 * forever bound value. The {@link #put(String, Object)} and {@link #remove(String)} operations will
 * replace both tick bound values and forever bound values.
 *
 * @author Elliot Ford
 *
 */
public interface EngineBindings {

    /**
     * Whether the bindings contains a mapping for the supplied key.
     *
     * @param key
     *            Key to check.
     * @return Whether there is a value associated to this key.
     */
    default boolean containsKey(final String key) {
	return get(key) != null;
    }

    /**
     * Gets the value bound to the supplied key.
     *
     * @param key
     *            Key to check.
     * @return Bound value or null if none found.
     */
    <T> T get(String key);

    /**
     * Binds the supplied key-value pair.
     *
     * @param key
     *            Key to bind value to.
     * @param value
     *            Value to bind.
     * @return Previously associated value or null if none were.
     */
    <T> T put(String key, T value);

    /**
     * Binds the supplied key-value pair to the current tick bindings.
     *
     * @param key
     *            Key to bind value to.
     * @param value
     *            Value to bind.
     * @return Previously associated value or null if none were.
     */
    <T> T putForTick(String key, T value);

    /**
     * Removes the value bound to the supplied key.
     *
     * @param key
     *            Key to check.
     * @return Value that was bound to the key or else null if none were.
     */
    <T> T remove(String key);
}