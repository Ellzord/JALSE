package jalse.tags;

import java.util.UUID;

import jalse.entities.EntityContainer;

/**
 * A {@link Tag} to show the origin {@link EntityContainer} ID for an {@link Entity} (as it can be
 * transfered).
 *
 * @author Elliot Ford
 *
 */
@SingletonTag
public final class OriginContainer extends AbstractValueTag<UUID> {

    /**
     * Creates a new origin container.
     *
     * @param id
     *            Origin ID.
     */
    public OriginContainer(final UUID id) {
	super(id);
    }
}
