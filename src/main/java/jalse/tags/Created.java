package jalse.tags;

import java.util.Date;

/**
 * A {@link Tag} to show the original creation date of a {@link Taggable}.
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public final class Created extends AbstractDateValueTag {

    /**
     * New created (now).
     */
    public Created() {}

    /**
     * New created with defined date.
     *
     * @param d
     *            Date to set.
     */
    public Created(final Date d) {
	super(d);
    }
}
