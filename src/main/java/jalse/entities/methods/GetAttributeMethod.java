package jalse.entities.methods;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.entities.Entity;
import jalse.entities.annotations.GetAttribute;
import jalse.entities.functions.GetAttributeFunction;

import java.util.Objects;

/**
 * This is used for mapping calls to: *
 * <ul>
 * <li>{@link AttributeContainer#getOptAttribute(String, AttributeType)}</li>
 * <li>{@link AttributeContainer#getAttribute(String, AttributeType)}</li>
 * </ul>
 *
 * @author Elliot Ford
 *
 * @see GetAttribute
 * @see GetAttributeFunction
 *
 */
public class GetAttributeMethod implements EntityMethod {

    private final String name;
    private final AttributeType<Object> type;
    private final boolean optional;

    /**
     * Creates a new get attribute method.
     *
     * @param name
     *            Attribute name.
     * @param type
     *            Attribute type.
     * @param optional
     *            Optional return type.
     */
    public GetAttributeMethod(final String name, final AttributeType<Object> type, final boolean optional) {
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
	if (args != null && args.length > 0) {
	    throw new IllegalArgumentException("Should have no arguments");
	}
	if (optional) {
	    return entity.getOptAttribute(name, type);
	} else {
	    return entity.getAttribute(name, type);
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
