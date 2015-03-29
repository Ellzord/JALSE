package jalse.listeners;

import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Attribute change event for {@link AttributeListener}. This is a unique event that contains the
 * relevant {@link AttributeType} value and it's parent {@link AttributeContainer} (even if the
 * Attribute has been removed). When an Attribute is replaced the previous Attribute is also
 * contained within the event.
 *
 * @author Elliot Ford
 * @param <T>
 *            Attribute type.
 *
 * @see AttributeContainer
 *
 */
public class AttributeEvent<T> extends AbstractIdentifiable {

    private final AttributeContainer container;
    private final AttributeType<T> type;
    private final T value;
    private final T replacedValue;

    /**
     * Creates a new AttributeEvent with a random ID (with no previous Attribute).
     *
     * @param container
     *            Parent container for the Attribute.
     * @param type
     *            Attribute type.
     * @param value
     *            Attribute the event is for.
     */
    public AttributeEvent(final AttributeContainer container, final AttributeType<T> type, final T value) {
	this(container, type, value, null);
    }

    /**
     * Creates a new AttributeEvent with a random ID.
     *
     * @param container
     *            Parent container for the Attribute.
     * @param type
     *            Attribute type.
     * @param value
     *            Attribute the event is for.
     * @param replacedValue
     *            The previous attribute that has been replaced by this Attribute (can be null).
     */
    public AttributeEvent(final AttributeContainer container, final AttributeType<T> type, final T value,
	    final T replacedValue) {
	super(UUID.randomUUID());
	this.container = Objects.requireNonNull(container);
	this.type = Objects.requireNonNull(type);
	this.value = Objects.requireNonNull(value);
	this.replacedValue = replacedValue;
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
    public T getOrNullReplacedValue() {
	return replacedValue;
    }

    /**
     * Get the Attribute that was replaced by this Attribute.
     *
     * @return Optional containing replaced attribute or empty optional if nothing was replaced.
     */
    public Optional<T> getReplacedValue() {
	return Optional.ofNullable(replacedValue);
    }

    /**
     * Gets the attribute type for the attribute.
     *
     * @return Attribute type.
     */
    public AttributeType<T> getType() {
	return type;
    }

    /**
     * Gets the Attribute the event is for.
     *
     * @return The relevant Attribute.
     */
    public T getValue() {
	return value;
    }

    /**
     * Checks whether the previous Attribute was replaced by this Attribute.
     *
     * @return Whether the previous Attribute was replaced.
     */
    public boolean isReplacement() {
	return replacedValue != null;
    }
}
