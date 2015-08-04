package jalse.tags;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used identify any class that is exposing internal state through {@link Tag}.
 *
 * @author Elliot Ford
 *
 * @see Tag
 * @see SingletonTag
 *
 */
public interface Taggable {

    /**
     * Gets a single tag from the tag collection.
     *
     * @param type
     *            Singleton tag type.
     * @return Single tag or null if not found.
     *
     * @see SingletonTag
     */
    @SuppressWarnings("unchecked")
    default <T extends Tag> T getSingletonTag(final Class<T> type) {
	if (!type.isAnnotationPresent(SingletonTag.class)) {
	    throw new IllegalArgumentException(String.format("%s is not marked with SingletonTag", type));
	}
	return (T) streamTags().filter(t -> type.equals(t.getClass())).findAny().orElse(null);
    }

    /**
     * Gets all tag state information.
     *
     * @return All tags or an empty set if none are found.
     *
     * @see #streamTags()
     */
    default Set<Tag> getTags() {
	return streamTags().collect(Collectors.toSet());
    }

    /**
     * Gets a tags of the defined type.
     *
     * @param type
     *            Tag type.
     *
     * @return Tags of type.
     */
    <T extends Tag> Set<T> getTagsOfType(Class<T> type);

    /**
     * Streams all tag state information.
     *
     * @return Stream of all tags.
     */
    Stream<Tag> streamTags();
}
