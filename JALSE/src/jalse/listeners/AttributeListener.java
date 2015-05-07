package jalse.listeners;

import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.attributes.DefaultAttributeContainer;

/**
 * Listener for {@link AttributeType} value manipulation. When attributes are added, updated or
 * removed the suitable defined method will be triggered. A change is a manual fire of the trigger
 * to denote a internal state change of the attribute. Unique {@link AttributeEvent} will be
 * supplied for each trigger.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type to trigger for.
 *
 * @see AttributeContainer
 * @see DefaultAttributeContainer
 *
 */
public interface AttributeListener<T> {

    /**
     * Triggered when an attribute has been added. If the attribute was replaced this will be
     * reflected in the event.
     *
     * @param event
     *            The attribute event for this trigger.
     */
    void attributeAdded(AttributeEvent<T> event);

    /**
     * Triggered when an attribute has been changed (either replacement by another attribute or an
     * internal state change).
     *
     * @param event
     *            The attribute event for this trigger.
     */
    void attributeChanged(AttributeEvent<T> event);

    /**
     * Triggered when an attribute has been removed.
     *
     * @param event
     *            The attribute event for this trigger.
     */
    void attributeRemoved(AttributeEvent<T> event);
}
