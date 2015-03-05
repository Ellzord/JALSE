package jalse.listeners;

import jalse.attributes.Attribute;
import jalse.attributes.AttributeContainer;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.UUID;

/**
 * Attribute change event for {@link AttributeListener}. This is a unique event
 * that contains the relevant {@link Attribute} and it's parent
 * {@link AttributeContainer} (even if the Attribute has been removed). When an
 * Attribute is replaced the previous Attribute is also contained within the
 * event.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type.
 *
 * @see AttributeContainer
 *
 */
public class AttributeEvent<T extends Attribute> extends AbstractIdentifiable {

    private final AttributeContainer container;
    private final T attr;
    private final T previousAttr;

    /**
     * Creates a new AttributeEvent with a random ID (with no previous
     * Attribute).
     *
     * @param container
     *            Parent container for the Attribute.
     * @param attr
     *            Attribute the event is for.
     */
    public AttributeEvent(final AttributeContainer container, final T attr) {

	this(container, attr, null);
    }

    /**
     * Creates a new AttributeEvent with a random ID.
     *
     * @param container
     *            Parent container for the Attribute.
     * @param attr
     *            Attribute the event is for.
     * @param previousAttr
     *            The previous attribute that has been replaced (can be null).
     */
    public AttributeEvent(final AttributeContainer container, final T attr, final T previousAttr) {

	super(UUID.randomUUID());

	this.container = Objects.requireNonNull(container);
	this.attr = Objects.requireNonNull(attr);
	this.previousAttr = previousAttr;
    }

    /**
     * Gets the Attribute the event is for.
     *
     * @return The relevant Attribute.
     */
    public T getAttribute() {

	return attr;
    }

    /**
     * Gets the Attribute's parent container.
     *
     * @return Parent container.
     */
    public AttributeContainer getContainer() {

	return container;
    }

    /**
     * Get the Attribute that was replaced.
     *
     * @return Replaced Attribute.
     */
    public T getPreviousAttribute() {

	return previousAttr;
    }

    /**
     * Checks whether the previous Attribute was replaced.
     *
     * @return Whether the previous Attribute was replaced.
     */
    public boolean isReplacement() {

	return previousAttr != null;
    }
}
