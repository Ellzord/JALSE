package jalse.entities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.entities.Entity;
import jalse.entities.functions.GetAttributeFunction;
import jalse.entities.methods.GetAttributeMethod;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link AttributeContainer#getOptAttribute(String, AttributeType)}</li>
 * <li>{@link AttributeContainer#getAttribute(String, AttributeType)}</li>
 * </ul>
 *
 * See {@link GetAttributeFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see GetAttributeMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetAttribute {

    /**
     * Default name ({@code ""}).
     */
    public static final String DEFAULT_NAME = "";

    /**
     * Attribute type name.
     *
     * @return Name.
     *
     * @see GetAttribute#DEFAULT_NAME
     */
    String name() default DEFAULT_NAME;
}
