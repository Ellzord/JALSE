package jalse.entities.methods;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.entities.Entity;
import jalse.entities.annotations.SetAttribute;
import jalse.entities.functions.SetAttributeFunction;

import java.util.Objects;

/**
 * This is used for mapping calls to:
 * <ul>
 * <li>{@link AttributeContainer#setOptAttribute(String, AttributeType, Object)}</li>
 * <li>{@link AttributeContainer#setAttribute(String, AttributeType, Object)}</li>
 * <li>{@link AttributeContainer#removeOptAttribute(String, AttributeType)}</li>
 * <li>{@link AttributeContainer#removeAttribute(String, AttributeType)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see SetAttribute
 * @see SetAttributeFunction
 *
 */
public class SetAttributeMethod implements EntityMethod {

    private final String name;
    private final AttributeType<Object> type;
    private final boolean optional;

    /**
     * Creates a new set method
     *
     * @param name
     *            Attribute name
     * @param type
     *            Attribute type
     * @param optional
     *            Optional return type.
     */
    public SetAttributeMethod(final String name, final AttributeType<Object> type, final boolean optional) {
	this.name = requireNotEmpty(name);
	this.type = Objects.requireNonNull(type);
	this.optional = optional;
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
     * Gets the attribute type.
     *
     * @return Attribute type.
     */
    public AttributeType<Object> getType() {
	return type;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Check no args
	if (args.length != 1) {
	    throw new IllegalArgumentException("Should have 1 argument");
	}
	if (optional) {
	    return entity.setOptAttribute(name, type, args[0]);
	} else {
	    return entity.setAttribute(name, type, args[0]);
	}
    }

    /**
     * Whether the optional equivelent will be used.
     *
     * @return Is an {@code opt} method.
     */
    public boolean isOptional() {
	return optional;
    }
}
