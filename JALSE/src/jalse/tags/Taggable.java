package jalse.tags;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Used identify any class that is exposing internal state through {@link Tag}.
 *
 * @author Elliot Ford
 *
 */
public interface Taggable {

    /**
     * Gets all tag state information.
     *
     * @return All tags or an empty set if none are found.
     */
    Set<Tag> getTags();

    /**
     * Streams all tag state information.
     *
     * @return Stream of all tags.
     */
    default Stream<Tag> streamTags() {
	return getTags().stream();
    }
}
