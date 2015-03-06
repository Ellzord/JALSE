package jalse.entities;

import jalse.entities.EntityVisitor.EntityVisitResult;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

class EntityWalker {

    private final EntityContainer container;
    private final EntityVisitor visitor;
    private final Deque<EntityWalker> walkers;
    private Iterator<Entity> iterator;
    private boolean ignoreSiblings;
    private boolean exited;

    EntityWalker(final EntityContainer container, final EntityVisitor visitor) {

	this.container = Objects.requireNonNull(container);
	this.visitor = Objects.requireNonNull(visitor);

	walkers = new ArrayDeque<>();
	exited = false;

	reset();
    }

    public void walk() {

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
		return;
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
	    if (result != EntityVisitResult.IGNORE_CHILDREN) {

		walkers.addLast(new EntityWalker(e, visitor));
	    }

	    return;
	}

	final EntityWalker current = walkers.peekFirst();

	/*
	 * Walk already visited child.
	 */
	if (current != null) {

	    current.walk();

	    if (current.exited) {

		reset();
		return;
	    }

	    /*
	     * Child has been fully traversed.
	     */
	    if (!current.isWalking()) {

		walkers.removeFirst();

		if (walkers.isEmpty()) {

		    reset();
		}
	    }
	}
    }

    public boolean isWalking() {

	return iterator != null;
    }

    public void reset() {

	iterator = null;
	walkers.clear();
    }

    public EntityContainer getContainer() {

	return container;
    }

    public EntityVisitor getVisitor() {

	return visitor;
    }
}
