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
    private Iterator<Entity> iterator;
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
	exited = false;

	reset();
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

    public boolean isWalking() {

	return iterator != null;
    }

    public void reset() {

	iterator = null;
	walkers.clear();
    }

    public Entity walk() {

	/*
	 * Start walking.
	 */
	if (iterator == null) {

	    iterator = container.streamEntities().iterator();
	    exited = false;
	}

	/*
	 * Visit direct children.
	 */
	if (!ignoreSiblings && iterator.hasNext()) {

	    final Entity e = iterator.next();

	    final EntityVisitResult result = visitor.visit(e);

	    if (result == EntityVisitResult.EXIT) {

		exited = true;
		reset();
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

		walkers.addLast(new EntityTreeWalker(e, maxDepth - 1, visitor));
	    }

	    return e;
	}

	final EntityTreeWalker child = walkers.peekFirst();
	Entity e = null;

	/*
	 * Walk already visited child.
	 */
	if (child != null) {

	    e = child.walk();

	    if (child.exited) {

		reset();
	    }
	    /*
	     * Child has been fully traversed.
	     */
	    else if (!child.isWalking()) {

		walkers.removeFirst();

		if (walkers.isEmpty()) {

		    reset();
		}
	    }
	}

	return e;
    }
}
