package jalse.listeners;

/**
 * An abstract adapter for {@link AttributeListener}. This is a convenience class for creating
 * attribute listeners that may not require a full implementation. All methods implemented by this
 * class are empty.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type to trigger for.
 *
 */
public abstract class AttributeAdapter<T> implements AttributeListener<T> {

    @Override
    public void attributeAdded(final AttributeEvent<T> event) {}

    @Override
    public void attributeChanged(final AttributeEvent<T> event) {}

    @Override
    public void attributeRemoved(final AttributeEvent<T> event) {}
}
