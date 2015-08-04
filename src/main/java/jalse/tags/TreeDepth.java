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
     * Creates a new tree depth.
     *
     * @param depth
     *            Depth to set.
     */
    public TreeDepth(final int depth) {
	super(depth);
    }
}