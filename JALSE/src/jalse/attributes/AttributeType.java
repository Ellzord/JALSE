package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Objects;

/**
 * AttributeTypes and their values can be considered the core data of the JALSE model. AttributeType
 * defines what type the data must be. {@link AttributeContainer} is an attribute container where
 * attributes can be stored and {@link AttributeListener} can be set to trigger on value updates.<br>
 * <br>
 * The attribute type name is used for uniqueness. <br>
 * <br>
 *
 * An example attribute type:
 *
 * <pre>
 * <code>
 * AttributeType{@code<Boolean>} at = Attributes.newBooleanType("scary");
 * 
 * attributes.addOfType(at, true); // Adding
 * boolean scary = attributes.getOfType(at); // Getting
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 * @param <T>
 *            Value type.
 *
 * @see AttributeContainer
 * @see AttributeListener
 * @see AttributeSet
 * @see Attributes
 *
 */
public final class AttributeType<T> {

    private final String name;
    private final Class<? extends T> type;

    AttributeType(final String name, final Class<? extends T> type) {
	if (name == null || name.length() == 0) {
	    throw new IllegalArgumentException("Must have a non-null and non-empty name");
	}
	this.name = name;
	this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean equals(final Object obj) {
	return this == obj || obj instanceof AttributeType<?> && this.name.equals(((AttributeType<?>) obj).name);
    }

    /**
     * Name of the attribute type (unsed for uniqueness).
     *
     * @return Type name.
     */
    public String getName() {
	return name;
    }

    /**
     * Gets the value type.
     *
     * @return Value type.
     */
    public Class<? extends T> getType() {
	return type;
    }

    @Override
    public int hashCode() {
	return Objects.hash(name);
    }

    @Override
    public String toString() {
	return "AttributeType [name=" + name + " type=" + type.getName() + "]";
    }
}
