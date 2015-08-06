package jalse.tags;

import java.util.Set;
import java.util.UUID;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.misc.Identifiable;

/**
 * A utility for {@link Tag} value related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Tags {

    /**
     * Gets or calculates the root container.
     *
     * @param container
     *            Container to check for.
     * @return Root container for the supplied container.
     */
    public static RootContainer getRootContainer(final EntityContainer container) {
	// Root must be identifable
	if (!(container instanceof Identifiable)) {
	    return null;
	}

	EntityContainer parent = container instanceof Entity ? ((Entity) container).getContainer() : null;
	UUID rootID = null;

	// Check not root
	if (parent == null) {
	    rootID = ((Identifiable) container).getID();
	}
	// Get parents root
	else if (container instanceof Taggable) {
	    final RootContainer parentRoot = ((Taggable) container).getSingletonTag(RootContainer.class);
	    if (parentRoot != null) {
		rootID = parentRoot.getValue();
	    }
	}

	// Find root
	if (rootID == null) {
	    // Get top level entity
	    while (parent instanceof Entity) {
		final Entity e = (Entity) parent;
		rootID = e.getID();
		parent = e.getContainer();
	    }
	    // Container root (null, container or JALSE)
	    if (!(parent instanceof Identifiable)) {
		return null;
	    }
	}

	return new RootContainer(rootID);
    }

    /**
     * Gets the {@link TreeDepth} for the supplied container.
     *
     * @param container
     *            Container to get depth for.
     * @return Tree depth tag.
     */
    public static TreeDepth getTreeDepth(final EntityContainer container) {
	// Must be identifiable to be in the tree
	if (!(container instanceof Identifiable)) {
	    return null;
	}

	EntityContainer parent = container instanceof Entity ? ((Entity) container).getContainer() : null;

	// Check not root
	if (parent == null) {
	    return TreeDepth.ROOT;
	}

	// Increment parents depth
	if (container instanceof Taggable) {
	    final TreeDepth parentDepth = ((Taggable) container).getSingletonTag(TreeDepth.class);
	    if (parentDepth != null) {
		return parentDepth;
	    }
	}

	int depth = 1;

	// Calculate entity tree depth
	while (parent instanceof Entity) {
	    parent = ((Entity) parent).getContainer();
	    depth++;
	}

	// Check if root is not null but identifable
	if (parent instanceof Identifiable) {
	    depth++;
	}

	return new TreeDepth(depth);
    }

    /**
     * Sets the correct {@link TreeMember} tag for the container.
     *
     * @param container
     *            Container to check for.
     * @return The member enum.
     */
    public static TreeMember getTreeMember(final EntityContainer container) {
	// Must be identifiable to be a tree member
	if (!(container instanceof Identifiable)) {
	    return null;
	}
	// Entity with no identifable parent (or JALSE).
	if (!(container instanceof Entity) || ((Entity) container).getContainer() == null) {
	    return TreeMember.ROOT;
	}
	// Entity with children
	else if (container.hasEntities()) {
	    return TreeMember.NODE;
	}
	// Entity with no children
	else {
	    return TreeMember.LEAF;
	}
    }

    /**
     * Sets the {@link Created} tag (now).
     *
     * @param tags
     *            Tag set.
     */
    public static void setCreated(final Set<Tag> tags) {
	tags.add(new Created());
    }

    /**
     * Sets the {@link LastUpdated} tag (now).
     *
     * @param tags
     *            Tag set.
     */
    public static void setLastUpdated(final Set<Tag> tags) {
	tags.add(new LastUpdated());
    }

    /**
     * Sets the origin container to the provided container (if {@link Identifiable}).
     *
     * @param tags
     *            Tag set.
     * @param container
     *            Possible origin container.
     */
    public static void setOriginContainer(final Set<Tag> tags, final EntityContainer container) {
	final UUID id = Identifiable.getID(container);
	if (id != null) {
	    tags.add(new OriginContainer(id));
	}
    }

    /**
     * Sets the {@link TreeDepth} of 0.
     *
     * @param tags
     *            Tag set.
     */
    public static void setRootDepth(final Set<Tag> tags) {
	tags.add(TreeDepth.ROOT);
    }

    /**
     * Sets {@link TreeMember#ROOT}.
     *
     * @param tags
     *            Tag set.
     */
    public static void setRootMember(final Set<Tag> tags) {
	tags.add(TreeMember.ROOT);
    }

    private Tags() {
	throw new UnsupportedOperationException();
    }
}
