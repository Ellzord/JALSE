package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.functions.GetEntitiesFunction;
import jalse.entities.methods.GetEntitiesMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link EntityContainer#getEntities()}</li>
 * <li>{@link EntityContainer#getEntitiesOfType(Class)}</li>
 * <li>{@link EntityContainer#getEntitiesAsType(Class)}</li>
 * </ul>
 * The method is selected based on whether type filtering is enabled ({@link #ofType()}). <br>
 * <br>
 * See {@link GetEntitiesFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see GetEntitiesMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetEntities {

    /**
     * Default type filtering ({@code true}).
     */
    public static final boolean DEFAULT_OF_TYPE = true;

    /**
     * Whether filtering of type is performed.
     *
     * @return Filter of type.
     *
     * @see GetEntities#DEFAULT_OF_TYPE
     */
    boolean ofType() default DEFAULT_OF_TYPE;
}
