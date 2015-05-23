package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.functions.UnmarkAsTypeFunction;
import jalse.entities.methods.UnmarkAsTypeMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for {@link Entity#unmarkAsType(Class)}.<br>
 * <br>
 * See {@link UnmarkAsTypeFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see UnmarkAsTypeMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnmarkAsType {

    /**
     * Entity type to unmark.
     *
     * @return Entity type.
     */
    Class<? extends Entity> value();
}
