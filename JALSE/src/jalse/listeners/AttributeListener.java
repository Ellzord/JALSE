package jalse.listeners;

import jalse.attributes.Attributable;
import jalse.attributes.Attribute;

/**
 * Listener for {@link Attribute} manipulation. When attributes are added,
 * updated or removed the suitable defined method will be triggered. A change is
 * a manual fire of the trigger to denote a internal state change of the
 * attribute.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type to trigger for.
 *
 * @see Attributable
 *
 */
public interface AttributeListener<T extends Attribute> {

    /**
     * Triggered when an attribute has been added.
     *
     * @param attr
     *            Attribute that was just added.
     */
    void attributeAdded(T attr);

    /**
     * Triggered when an attribute has been changed (either replacement by
     * another attribute or an internal state change).
     *
     * @param attr
     *            Attribute that was just changed.
     */
    void attributeChanged(T attr);

    /**
     * Triggered when an attribute has been removed.
     *
     * @param attr
     *            Attribute that was just removed.
     */
    void attributeRemoved(T attr);
}
