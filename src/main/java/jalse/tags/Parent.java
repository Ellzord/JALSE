package jalse.tags;

import java.util.UUID;

import jalse.misc.AbstractIdentifiable;

/**
 * An immutable {@link Tag} used to identify a direct parent.
 *
 * @author Elliot Ford
 *
 */
public final class Parent extends AbstractIdentifiable implements Tag {

    /**
     * @param id
     *            ID of the parent.
     */
    public Parent(final UUID id) {
	super(id);
    }
}
