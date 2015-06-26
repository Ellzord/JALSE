package jalse.tags;

import java.util.Set;
import java.util.stream.Collectors;
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
     * 
     * @see #streamTags()
     */
    default Set<Tag> getTags() {
	return streamTags().collect(Collectors.toSet());
    }

    /**
     * Streams all tag state information.
     *
     * @return Stream of all tags.
     */
    Stream<Tag> streamTags();
}
