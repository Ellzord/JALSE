package jalse.entities.methods;

import static jalse.entities.functions.Functions.defaultValue;
import static jalse.entities.functions.Functions.toClass;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.attributes.NamedAttributeType;
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

    private final NamedAttributeType<Object> namedType;
    private final boolean primitive;
    private final boolean optional;
    private final Object defaultValue;

    /**
     * Creates a new set method
     *
     * @param namedType
     *            Named attribute type.
     * @param primitive
     *            Whether this is a primitive conversion.
     * @param optional
     *            Optional return type.
     */
    public SetAttributeMethod(final NamedAttributeType<Object> namedType, boolean primitive, final boolean optional) {
	this.namedType = Objects.requireNonNull(namedType);
	this.primitive = primitive;
	this.optional = optional;
	defaultValue = primitive ? defaultValue(toClass(namedType.getType().getValueType())) : null;
    }

    /**
     * Whether this is a primitive conversion.
     * 
     * @return Whether this is primitive.
     */
    public boolean isPrimitive() {
	return primitive;
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
    public NamedAttributeType<?> getNamedType() {
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
	if (args.length != 1) {
	    throw new IllegalArgumentException("Should have 1 argument");
	}
	if (optional) {
	    return entity.setOptAttribute(namedType, args[0]);
	} else {
	    Object result = entity.setAttribute(namedType, args[0]);
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
}
