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
	    throw new IllegalArgumentException("Must have a non-empty name");
	}
	this.name = name;
	this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof AttributeType<?>)) {
	    return false;
	}
	final AttributeType<?> other = (AttributeType<?>) obj;
	return name.equals(other.name) && type.equals(other.type);
    }

    /**
     * Name of the attribute type.
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

    /**
     * Whether the value would be accepted by this type.
     *
     * @param obj
     *            Value.
     * @return Whether the value is accepted.
     *
     * @see #isAcceptableValueType(Class)
     */
    public boolean isAcceptableValue(final Object obj) {
	return isAcceptableValueType(obj.getClass());
    }

    /**
     * Whether the value type is an accepted type.
     *
     * @param clazz
     *            Value type to check.
     * @return Whether the type was accepted.
     *
     * @see Class#isAssignableFrom(Class)
     */
    public boolean isAcceptableValueType(final Class<?> clazz) {
	return type.isAssignableFrom(clazz);
    }

    /**
     * Whether the attribute type is a subclass of this one.
     *
     * @param at
     *            Attribute type to check.
     * @return Whether the supplied type is a subclass.
     */
    public boolean isSubtype(final AttributeType<?> at) {
	return name.equals(at.name) && isAcceptableValueType(at.type);
    }

    @Override
    public String toString() {
	return "AttributeType [name=" + name + " type=" + type.getName() + "]";
    }
}
