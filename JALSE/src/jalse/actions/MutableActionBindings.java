package jalse.actions;

import java.util.Map;

/**
 * A mutable extension of {@link ActionBindings}. This can be supplied at the appropriate level for
 * editable bindings.
 *
 * @author Elliot Ford
 *
 */
public interface MutableActionBindings extends ActionBindings {

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
}
