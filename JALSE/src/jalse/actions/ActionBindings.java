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
     * Maps the key-values combinations.
     *
     * @return Map of bindings key-value combinations.
     */
    Map<String, ?> toMap();
}