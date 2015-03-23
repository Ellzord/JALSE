package jalse.listeners;

import jalse.attributes.Attribute;
import jalse.attributes.AttributeContainer;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Attribute change event for {@link AttributeListener}. This is a unique event that contains the
 * relevant {@link Attribute} and it's parent {@link AttributeContainer} (even if the Attribute has
 * been removed). When an Attribute is replaced the previous Attribute is also contained within the
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
    private final T replacedAttr;

    /**
     * Creates a new AttributeEvent with a random ID (with no previous Attribute).
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
     * @param replacedAttr
     *            The previous attribute that has been replaced by this Attribute (can be null).
     */
    public AttributeEvent(final AttributeContainer container, final T attr, final T replacedAttr) {
	super(UUID.randomUUID());
	this.container = Objects.requireNonNull(container);
	this.attr = Objects.requireNonNull(attr);
	this.replacedAttr = replacedAttr;
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
     * Get the Attribute that was replaced by this Attribute.
     *
     * @return Replaced Attribute or null if nothing was replaced.
     */
    public T getOrNullReplacedAttribite() {
	return replacedAttr;
    }

    /**
     * Get the Attribute that was replaced by this Attribute.
     *
     * @return Optional containing replaced attribute or empty optional if nothing was replaced.
     */
    public Optional<T> getReplacedAttribute() {
	return Optional.ofNullable(replacedAttr);
    }

    /**
     * Checks whether the previous Attribute was replaced by this Attribute.
     *
     * @return Whether the previous Attribute was replaced.
     */
    public boolean isReplacement() {
	return replacedAttr != null;
    }
}
