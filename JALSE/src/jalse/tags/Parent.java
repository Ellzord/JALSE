package jalse.tags;

import jalse.misc.AbstractIdentifiable;

import java.util.UUID;

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
