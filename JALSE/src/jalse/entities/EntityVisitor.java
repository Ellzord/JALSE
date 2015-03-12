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
 */
@FunctionalInterface
public interface EntityVisitor {

    /**
     * Different results for visiting an {@link Entity} (will affect entity walking).
     *
     * @author Elliot Ford
     *
     */
    enum EntityVisitResult {

	CONTINUE, EXIT, IGNORE_CHILDREN, IGNORE_SIBLINGS
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
