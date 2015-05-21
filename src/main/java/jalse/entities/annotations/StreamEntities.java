package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.functions.StreamEntitiesFunction;
import jalse.entities.methods.StreamEntitiesMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link EntityContainer#streamEntities()}</li>
 * <li>{@link EntityContainer#streamEntitiesOfType(Class)}</li>
 * <li>{@link EntityContainer#streamEntitiesAsType(Class)}</li>
 * </ul>
 * The method is selected based on whether type filtering is enabled ({@link #ofType()}). <br>
 * <br>
 * See {@link StreamEntitiesFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see StreamEntitiesMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StreamEntities {

    /**
     * Default type filtering ({@code true}).
     *
     * @see #ofType()
     */
    public static final boolean DEFAULT_OF_TYPE = true;

    /**
     * Whether filtering of type is performed.
     *
     * @return Filter of type.
     */
    boolean ofType() default DEFAULT_OF_TYPE;
}
