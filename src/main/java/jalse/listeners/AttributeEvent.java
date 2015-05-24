package jalse.listeners;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.attributes.NamedAttributeType;
import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.Optional;

/**
 * Attribute change event for {@link AttributeListener}. This is a unique event that contains the
 * relevant {@link AttributeType} value and it's parent {@link AttributeContainer} (even if the
 * Attribute has been removed). When an attribute is replaced the previous attribute is also
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
    private final String name;
    private final AttributeType<T> type;
    private final T value;
    private final T replacedValue;

    /**
     * Creates a new AttributeEvent with a random ID (with no previous Attribute).
     *
     * @param container
     *            Parent container for the Attribute.
     * @param name
     *            Attribute name.
     * @param type
     *            Attribute type.
     * @param value
     *            Attribute the event is for.
     */
    public AttributeEvent(final AttributeContainer container, final String name, final AttributeType<T> type,
	    final T value) {
	this(container, name, type, value, null);
    }

    /**
     * Creates a new AttributeEvent with a random ID.
     *
     * @param container
     *            Parent container for the Attribute.
     * @param name
     *            Attribute name.
     * @param type
     *            Attribute type.
     * @param value
     *            Attribute the event is for.
     * @param replacedValue
     *            The previous attribute that has been replaced by this Attribute (can be null).
     */
    public AttributeEvent(final AttributeContainer container, final String name, final AttributeType<T> type,
	    final T value, final T replacedValue) {
	this.name = requireNotEmpty(name);
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
     * Gets the attribute name.
     *
     * @return Attribute name.
     */
    public String getName() {
	return name;
    }

    /**
     * Gets the named attribute type.
     *
     * @return Named attribute type.
     */
    public NamedAttributeType<T> getNamedType() {
	return new NamedAttributeType<>(name, type);
    }

    /**
     * Get the Attribute that was replaced by this Attribute.
     *
     * @return Optional containing replaced attribute or empty optional if nothing was replaced.
     */
    public Optional<T> getOptReplacedValue() {
	return Optional.ofNullable(replacedValue);
    }

    /**
     * Get the Attribute that was replaced by this Attribute.
     *
     * @return Replaced Attribute or null if nothing was replaced.
     */
    public T getReplacedValue() {
	return replacedValue;
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
