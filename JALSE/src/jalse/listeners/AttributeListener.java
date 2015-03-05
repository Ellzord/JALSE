package jalse.listeners;

import static jalse.misc.TypeParameterResolver.getTypeParameter;
import jalse.attributes.Attribute;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeSet;

import java.lang.reflect.TypeVariable;

/**
 * Listener for {@link Attribute} manipulation. When attributes are added,
 * updated or removed the suitable defined method will be triggered. A change is
 * a manual fire of the trigger to denote a internal state change of the
 * attribute. Unique {@link AttributeEvent} will be supplied for each trigger.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type to trigger for.
 *
 * @see AttributeContainer
 * @see AttributeSet
 *
 */
public interface AttributeListener<T extends Attribute> {

    /**
     * The {@code T extends Attribute} parameter.
     */
    TypeVariable<? extends Class<?>> TYPE_PARAMETER = getTypeParameter(AttributeListener.class, "T");

    /**
     * Triggered when an attribute has been added. If the attribute was replaced
     * this will be reflected in the event.
     *
     * @param event
     *            The attribute event for this trigger.
     */
    void attributeAdded(AttributeEvent<T> event);

    /**
     * Triggered when an attribute has been changed (either replacement by
     * another attribute or an internal state change).
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
