package jalse.tags;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link Tag} value implementation for {@link Date} (that formats ({@link #toString()} correctly
 * and safely).
 *
 * @author Elliot Ford
 *
 * @see ThreadLocal
 * @see SimpleDateFormat
 *
 */
public abstract class AbstractDateValueTag extends AbstractValueTag<Date> {

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {

	@Override
	protected SimpleDateFormat initialValue() {
	    return new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
	};
    };

    /**
     * Creates a new date tag (now).
     */
    public AbstractDateValueTag() {
	this(new Date());
    }

    /**
     * Creates a new date tag for a set date.
     *
     * @param d
     *            Date to set.
     */
    public AbstractDateValueTag(final Date d) {
	super(d);
    }

    /**
     * Wrapper method for getting the date.
     *
     * @return The associated date value.
     *
     * @see #getValue()
     */
    public Date getDate() {
	return getValue();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [" + sdf.get().format(getDate()) + "]";
    }
}
