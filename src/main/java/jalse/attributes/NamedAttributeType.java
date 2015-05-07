package jalse.attributes;

import static jalse.attributes.Attributes.requireNotEmpty;

import java.util.Objects;

/**
 * A named {@link AttributeType} for defining name-type combinations. Attribute type names must be
 * non-empty.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type the AttributeType is representing.
 *
 * @see AttributeType
 */
public final class NamedAttributeType<T> {

    private final String name;
    private final AttributeType<T> type;

    /**
     * Creates a new named attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     */
    public NamedAttributeType(final String name, final AttributeType<T> type) {
	this.name = requireNotEmpty(name);
	this.type = Objects.requireNonNull(type);
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}

	if (!(obj instanceof NamedAttributeType<?>)) {
	    return false;
	}

	final NamedAttributeType<?> other = (NamedAttributeType<?>) obj;
	return name.equals(other.name) && type.equals(other.type);
    }

    /**
     * Gets the attribute type name.
     *
     * @return Name.
     */
    public String getName() {
	return name;
    }

    /**
     * Gets the attribute type.
     *
     * @return Attribute type.
     */
    public AttributeType<T> getType() {
	return type;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + name.hashCode();
	result = prime * result + type.hashCode();
	return result;
    }

    @Override
    public String toString() {
	return "NamedAttributeType [name=" + name + " type=" + type + "]";
    }
}
