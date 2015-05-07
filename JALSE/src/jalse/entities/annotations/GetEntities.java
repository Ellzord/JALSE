package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for {@link EntityContainer#getEntitiesOfType(Class)} and
 * {@link EntityContainer#getEntitiesAsType(Class)}.<br>
 * <br>
 * The method is selected based on whether type filtering is enabled ({@link #ofType()}).
 *
 * @author Elliot Ford
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetEntities {

    /**
     * Whether filtering of type is performed.
     *
     * @return Filter of type.
     */
    boolean ofType() default true;
}
