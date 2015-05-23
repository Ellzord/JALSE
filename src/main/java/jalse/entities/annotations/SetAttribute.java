package jalse.entities.annotations;

import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.entities.Entity;
import jalse.entities.functions.SetAttributeFunction;
import jalse.entities.methods.SetAttributeMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link AttributeContainer#setOptAttribute(String, AttributeType, Object)}</li>
 * <li>{@link AttributeContainer#setAttribute(String, AttributeType, Object)}</li>
 * <li>{@link AttributeContainer#removeOptAttribute(String, AttributeType)}</li>
 * <li>{@link AttributeContainer#removeAttribute(String, AttributeType)}</li>
 * </ul>
 *
 * See {@link SetAttributeFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see SetAttributeMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SetAttribute {

    /**
     * Default name ({@code ""}).
     */
    public static final String DEFAULT_NAME = "";

    /**
     * Attribute type name.
     *
     * @return Name.
     *
     * @see SetAttribute#DEFAULT_NAME
     */
    String name() default DEFAULT_NAME;
}
