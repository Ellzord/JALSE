package jalse.entities.methods;

import static jalse.entities.functions.Functions.defaultValue;
import static jalse.entities.functions.Functions.toClass;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.attributes.NamedAttributeType;
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

    private final NamedAttributeType<Object> namedType;
    private final boolean primitive;
    private final boolean optional;
    private final Object defaultValue;

    /**
     * Creates a new get attribute method.
     *
     * @param namedType
     *            Named attribute type.
     * @param optional
     *            Optional return type.
     */
    public GetAttributeMethod(final NamedAttributeType<Object> namedType, final boolean primitive,
	    final boolean optional) {
	this.namedType = Objects.requireNonNull(namedType);
	this.primitive = primitive;
	this.optional = optional;
	defaultValue = primitive ? defaultValue(toClass(namedType.getType().getValueType())) : null;
    }

    /**
     * Gets the attribute name.
     *
     * @return Attribute name.
     */
    public String getName() {
	return namedType.getName();
    }

    /**
     * Gets the named attribute type.
     *
     * @return Named attribute type.
     */
    public NamedAttributeType<Object> getNamedType() {
	return namedType;
    }

    /**
     * Gets the attribute type.
     *
     * @return Attribute type.
     */
    public AttributeType<?> getType() {
	return namedType.getType();
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Check no args
	if (args != null && args.length > 0) {
	    throw new IllegalArgumentException("Should have no arguments");
	}
	if (optional) {
	    return entity.getOptAttribute(namedType);
	} else {
	    Object result = entity.getAttribute(namedType);
	    if (result == null && primitive) {
		result = defaultValue;
	    }
	    return result;
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

    /**
     * Whether this is a primitive conversion.
     *
     * @return Whether this is primitive.
     */
    public boolean isPrimitive() {
	return primitive;
    }
}
