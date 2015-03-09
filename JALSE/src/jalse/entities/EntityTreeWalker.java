package jalse.entities;

import jalse.entities.EntityVisitor.EntityVisitResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

class EntityTreeWalker {

    private final EntityContainer container;
    private final int maxDepth;
    private final EntityVisitor visitor;
    private final Deque<EntityTreeWalker> walkers;
    private final Iterator<Entity> iterator;
    private boolean ignoreSiblings;
    private boolean exited;

    EntityTreeWalker(final EntityContainer container, final int maxDepth, final EntityVisitor visitor) {
	this.container = Objects.requireNonNull(container);
	this.visitor = Objects.requireNonNull(visitor);

	if (maxDepth <= 0) {
	    throw new IllegalArgumentException();
	}
	this.maxDepth = maxDepth;

	walkers = new ArrayDeque<>();
	iterator = container.streamEntities().iterator();
	exited = false;
    }

    private boolean canAddChild() {
	return !ignoreSiblings && iterator.hasNext();
    }

    private boolean canWalkChild() {
	return !walkers.isEmpty();
    }

    public EntityContainer getContainer() {
	return container;
    }

    public int getMaxDepth() {
	return maxDepth;
    }

    public EntityVisitor getVisitor() {
	return visitor;
    }

    private boolean hasExited() {
	return exited;
    }

    public boolean isWalking() {
	return !hasExited() && (canAddChild() || canWalkChild());
    }

    public Entity walk() {
	if (!isWalking()) {
	    throw new IllegalStateException();
	}

	/*
	 * Visit direct children.
	 */
	if (canAddChild()) {
	    final Entity e = iterator.next();
	    final EntityVisitResult result = visitor.visit(e);
	    if (result == EntityVisitResult.EXIT) {

		exited = true;
		return e;
	    }

	    /*
	     * Remove other children.
	     */
	    if (result == EntityVisitResult.IGNORE_SIBLINGS) {
		ignoreSiblings = true;
		walkers.clear();
	    }

	    /*
	     * Do not walk through child descendants.
	     */
	    if (maxDepth > 1 && result != EntityVisitResult.IGNORE_CHILDREN) {
		final EntityTreeWalker child = new EntityTreeWalker(e, maxDepth - 1, visitor);
		if (child.isWalking()) {
		    walkers.addLast(child);
		}
	    }

	    return e;
	}

	/*
	 * Walk already visited child.
	 */
	final EntityTreeWalker child = walkers.peekFirst();
	final Entity e = child.walk();

	/*
	 * Child has been fully traversed.
	 */
	if (!child.isWalking()) {
	    walkers.removeFirst();
	}

	return e;
    }

}
