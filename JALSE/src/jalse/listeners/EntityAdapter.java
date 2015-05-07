package jalse.listeners;

/**
 * An {@code abstract} adapter for {@link EntityListener}. This is a convenience class for creating
 * entity listeners that may not require a full implementation. All methods implemented by this
 * class are empty.
 *
 * @author Elliot Ford
 *
 */
public abstract class EntityAdapter implements EntityListener {

    @Override
    public void entityCreated(final EntityEvent event) {}

    @Override
    public void entityKilled(final EntityEvent event) {}

    @Override
    public void entityReceived(final EntityEvent event) {}

    @Override
    public void entityTransferred(final EntityEvent event) {}
}
