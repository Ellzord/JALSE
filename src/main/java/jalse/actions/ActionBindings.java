package jalse.actions;

import java.util.Map;

/**
 * An immutable key-value bindings for {@link ActionEngine} (keys are strings).
 *
 * @author Elliot Ford
 *
 */
public interface ActionBindings {

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
     * Binds all of the key-value pairs in the supplied map.
     *
     * @param map
     *            Key-value pairs to bind.
     */
    void putAll(Map<String, ?> map);

    /**
     * Removes the value bound to the supplied key.
     *
     * @param key
     *            Key to check.
     * @return Value that was bound to the key or else null if none were.
     */
    <T> T remove(String key);

    /**
     * Removes all key-value pairs.
     */
    void removeAll();

    /**
     * Maps the key-values combinations.
     *
     * @return Map of bindings key-value combinations.
     */
    Map<String, ?> toMap();
}