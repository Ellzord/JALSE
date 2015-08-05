package jalse.tags;

import jalse.entities.EntityContainer;

/**
 * A {@link Tag} used to show what tree member type an {@link EntityContainer} is.
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public enum TreeMember implements Tag {

    /**
     * Root of the tree (origin).
     */
    ROOT,

    /**
     * A tree member with child nodes.
     */
    NODE,

    /**
     * A tree member with no child nodes (that is not the root).
     */
    LEAF
}
