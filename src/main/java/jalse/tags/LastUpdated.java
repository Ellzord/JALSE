package jalse.tags;

import java.util.Date;

/**
 * A {@link Tag} to show the last updated date of a {@link Taggable}.
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public final class LastUpdated extends AbstractDateValueTag {

    /**
     * New last updated (now).
     */
    public LastUpdated() {}

    /**
     * New last updated with defined date.
     *
     * @param d
     *            Date to set.
     */
    public LastUpdated(final Date d) {
	super(d);
    }
}
