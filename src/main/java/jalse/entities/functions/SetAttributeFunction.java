package jalse.entities.functions;

import static jalse.attributes.Attributes.newNamedUnknownType;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.isPrimitive;
import static jalse.entities.functions.Functions.returnTypeIs;
import jalse.attributes.AttributeContainer;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.annotations.SetAttribute;
import jalse.entities.methods.SetAttributeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * This is a method function for {@link SetAttribute} annotation. It will resolve an
 * {@link SetAttributeMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to
 * {@link AttributeContainer#setAttribute(String, jalse.attributes.AttributeType, Object)} supplied
 * with the name {@code scary}, type {@code Boolean} and the specified input.
 *
 * <pre>
 * <code>
 * {@code @SetAttribute(name = "scary")}
 * Boolean setScary(Boolean scary);
 * 
 * {@code @SetAttribute}
 * void setScary(Boolean scary);
 * </code>
 * </pre>
 *
 * The next example signature will resolve to
 * {@link AttributeContainer#setOptAttribute(String, jalse.attributes.AttributeType, Object)}
 * supplied with the name {@code scary}, type {@code Boolean} and the specified input.
 *
 * <pre>
 * <code>
 * {@code @SetAttribute(name = "scary")}
 * Optional{@code <Boolean>} setScary(Boolean scary);
 * </code>
 * </pre>
 *
 * When {@code null} is supplied as an input to the above signatures it will be translated to
 * {@link AttributeContainer#removeAttribute(String, jalse.attributes.AttributeType)}. <br>
 * <br>
 * NOTE: This function will throw exceptions if {@link SetAttribute} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class SetAttributeFunction implements EntityMethodFunction {

    private static final String SET_PREFIX = "set";

    @Override
    public SetAttributeMethod apply(final Method m) {
	// Check for annotation
	final SetAttribute annonation = m.getAnnotation(SetAttribute.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkNotDefault(m);
	if (m.getParameterCount() != 1) {
	    throw new IllegalArgumentException("Must have only one param");
	}

	// Work out attribute name
	String name = annonation.name();
	if (name.length() == 0) {
	    String methodName = m.getName();
	    if (methodName.startsWith(SET_PREFIX)) {
		// setAmount -> Amount
		methodName = methodName.substring(SET_PREFIX.length());
	    }
	    // Amount -> amount
	    name = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
	}

	// Check suitable name
	if (name.length() == 0) {
	    // Must have a name.
	    throw new IllegalArgumentException("Attribute name is empty");
	}

	final Type attrType = m.getGenericParameterTypes()[0];

	// Check not primitive
	if (isPrimitive(attrType)) {
	    throw new IllegalArgumentException("Attribute types cannot be primitive (use wrappers)");
	}

	// Check return type matches.
	final boolean optional = false;
	if (hasReturnType(m)) {
	    Type returnAttrType = m.getGenericReturnType();

	    // Check optional
	    if (returnTypeIs(m, Optional.class)) {
		returnAttrType = firstGenericTypeArg(returnAttrType);
	    }

	    // Check types match
	    if (!attrType.equals(returnAttrType)) {
		throw new IllegalArgumentException("Both parameter and return attribute types should match");
	    }
	}

	// Create set attribute method
	return new SetAttributeMethod(newNamedUnknownType(name, attrType), optional);
    }
}
