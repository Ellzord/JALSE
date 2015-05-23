package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.functions.MarkAsTypeFunction;
import jalse.entities.methods.MarkAsTypeMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for {@link Entity#markAsType(Class)}.<br>
 * <br>
 * See {@link MarkAsTypeFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see MarkAsTypeMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkAsType {

    /**
     * Entity type to mark.
     *
     * @return Entity type.
     */
    Class<? extends Entity> value();
}
