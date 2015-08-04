package jalse.tags;

import java.util.UUID;

import jalse.entities.EntityContainer;

/**
 * A {@link Tag} to show the root {@link EntityContainer} ID.
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public final class RootContainer extends AbstractValueTag<UUID> {

    /**
     * Creates a new root container.
     *
     * @param id
     *            Roots ID.
     */
    public RootContainer(final UUID id) {
	super(id);
    }
}
