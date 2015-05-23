package jalse.entities.functions;

import static jalse.attributes.Attributes.newUnknownType;
import static jalse.entities.functions.Functions.checkHasReturnType;
import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.isPrimitive;
import static jalse.entities.functions.Functions.returnTypeIs;
import jalse.attributes.AttributeContainer;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.annotations.GetAttribute;
import jalse.entities.methods.GetAttributeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * This is a method function for {@link GetAttribute} annotation. It will resolve an
 * {@link GetAttributeMethod} to be used by the entity typing system.<br>
 * <br>
 * The next example signatures will resolve to
 * {@link AttributeContainer#getAttribute(String, jalse.attributes.AttributeType)} supplied with the
 * name {@code scary} and type {@code Boolean}.
 *
 * <pre>
 * <code>
 * {@code @GetAttribute(name = "scary")}
 * Boolean isScary();
 * 
 * {@code @GetAttribute}
 * Boolean isScary();
 * </code>
 * </pre>
 *
 * The next example signatures will resolve to
 * {@link AttributeContainer#getOptAttribute(String, jalse.attributes.AttributeType)} supplied with
 * the name {@code scary} and type {@code Boolean}.
 *
 * <pre>
 * <code>
 * {@code @GetAttribute(name = "scary")}
 * Optional{@code <Boolean>} isScary();
 * 
 * {@code @GetAttribute}
 * Optional{@code <Boolean>} isScary();
 * </code>
 * </pre>
 *
 * NOTE: This function will throw exceptions if {@link GetAttribute} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class GetAttributeFunction implements EntityMethodFunction {

    private static final String IS_PREFIX = "is";

    private static final String GET_PREFIX = "get";

    @Override
    public GetAttributeMethod apply(final Method m) {
	// Check for annotation
	final GetAttribute annonation = m.getAnnotation(GetAttribute.class);
	if (annonation == null) {
	    return null;
	}

	// Basic check method signature
	checkHasReturnType(m);
	checkNotDefault(m);
	checkNoParams(m);

	// Work out attribute name
	String name = annonation.name();
	if (name.length() == 0) {
	    String methodName = m.getName();
	    if (methodName.startsWith(GET_PREFIX)) {
		// getAmmount -> Ammount
		methodName = methodName.substring(GET_PREFIX.length());
	    } else if (methodName.startsWith(IS_PREFIX)) {
		// isScary -> Scary
		methodName = methodName.substring(IS_PREFIX.length());
	    }
	    // Scary -> scary
	    name = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
	}

	// Check suitable name
	if (name.length() == 0) {
	    // Must have a name.
	    throw new IllegalArgumentException("Attribute name is empty");
	}

	Type attrType = m.getGenericReturnType();

	// Check not primitive
	if (isPrimitive(attrType)) {
	    throw new IllegalArgumentException("Attribute types cannot be primitive (use wrappers)");
	}

	// Check is optional
	final boolean optional = returnTypeIs(m, Optional.class);
	if (optional) {
	    attrType = firstGenericTypeArg(attrType);
	}

	// Create get attribute method
	return new GetAttributeMethod(name, newUnknownType(attrType), optional);
    }
}
