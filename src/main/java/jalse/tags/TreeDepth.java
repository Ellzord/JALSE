package jalse.tags;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

/**
 * A {@link Tag} used to show the depth in a tree an {@link Entity} or {@link EntityContainer} may
 * be.
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public final class TreeDepth extends AbstractValueTag<Integer> {

    /**
     * Tree depth of zero.
     */
    public static final TreeDepth ROOT = new TreeDepth(0);

    /**
     * Creates a new tree depth.
     *
     * @param depth
     *            Depth to set.
     */
    public TreeDepth(final int depth) {
	super(depth);
	if (depth < 0) {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Creates an decremented tree depth.
     *
     * @return a new decremented tree depth.
     */
    public TreeDepth decrement() {
	return new TreeDepth(getValue() - 1);
    }

    /**
     * Creates an incremented tree depth.
     *
     * @return a new incremented tree depth.
     */
    public TreeDepth increment() {
	return new TreeDepth(getValue() + 1);
    }
}