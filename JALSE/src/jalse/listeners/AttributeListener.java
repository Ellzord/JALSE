package jalse.listeners;

import jalse.Core;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;

/**
 * Listener for {@link Attribute} manipulation. When attributes are added,
 * updated, removed or replaced the suitable defined method will be triggered. A
 * change is a manual fire of the trigger to denote a internal state change of
 * the attribute.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type to trigger for.
 *
 * @see Attributable
 * @see Core#fireAttributeChanged(Class)
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
     * Triggered when an attribute has been replaced.
     * 
     * @param newAttr
     *            The attribute replacing the original attribute.
     * @param oldAttr
     *            The original attribute that was replaced.
     */
    void attributeReplaced(T newAttr, T oldAttr);

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
