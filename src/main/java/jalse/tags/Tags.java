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
	EntityContainer parent = container instanceof Entity ? ((Entity) container).getContainer() : null;
	UUID rootID = null;

	// Check not root
	if (parent == null) {
	    rootID = Identifiable.getID(container);
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
	    // Container root
	    if (parent != null) {
		rootID = Identifiable.getID(parent);
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
	EntityContainer parent = container instanceof Entity ? ((Entity) container).getContainer() : null;

	// Check not root
	if (parent == null) {
	    return new TreeDepth(0);
	}

	// Increment parents depth
	if (container instanceof Taggable) {
	    final TreeDepth parentDepth = ((Taggable) container).getSingletonTag(TreeDepth.class);
	    if (parentDepth != null) {
		return parentDepth;
	    }
	}

	// Calculate depth
	int depth = 1;
	while (parent instanceof Entity) {
	    parent = ((Entity) parent).getContainer();
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
	if (!(container instanceof Entity) || ((Entity) container).getContainer() == null) {
	    return TreeMember.ROOT;
	} else if (container.hasEntities()) {
	    return TreeMember.NODE;
	} else {
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
	if (container instanceof Identifiable) {
	    tags.add(new OriginContainer(Identifiable.getID(container)));
	}
    }

    /**
     * Sets the {@link TreeDepth} of 0.
     *
     * @param tags
     *            Tag set.
     */
    public static void setRootDepth(final Set<Tag> tags) {
	tags.add(new TreeDepth(0));
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
