package jalse.entities;

/**
 * EntityVisitor allows {@link Entity} within an {@link EntityContainer} to be walked through one by
 * one recursively. These are walked through breadth-first and their walking can be filtered by
 * suppling different {@link EntityVisitResult}.<br>
 * <br>
 * 1. {@link EntityVisitResult#CONTINUE} the walker will continue as normal. <br>
 * 2. {@link EntityVisitResult#IGNORE_CHILDREN} the walker will not walk through any children of
 * this entity.<br>
 * 3. {@link EntityVisitResult#IGNORE_SIBLINGS} the walker will ignore all new siblings (only
 * processing the current entity and it's children).<br>
 * 4. {@link EntityVisitResult#EXIT} the walker will exit walking and reset.<br>
 * <br>
 *
 * Entities can be walked through using
 * {@link Entities#walkEntityTree(EntityContainer, int, EntityVisitor)}.
 *
 * @author Elliot Ford
 *
 * @see Entities
 *
 */
@FunctionalInterface
public interface EntityVisitor {

    /**
     * Result to be returned when visiting an {@link Entity}. The result may alter how the walker
     * processes the rest of the tree.
     *
     * @author Elliot Ford
     *
     */
    public enum EntityVisitResult {

	/**
	 * Continue to process this sub-tree.
	 */
	CONTINUE,

	/**
	 * Exit all sub-trees.
	 */
	EXIT,

	/**
	 * Make this member a leaf.
	 */
	IGNORE_CHILDREN,

	/**
	 * Only continue this sub-tree.
	 */
	IGNORE_SIBLINGS
    }

    /**
     * The walker visiting this entity.
     *
     * @param e
     *            Entity to visit.
     * @return Result of the visit.
     */
    EntityVisitResult visit(Entity e);
}
