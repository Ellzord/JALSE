package jalse.tags;

import java.util.Set;

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

}
