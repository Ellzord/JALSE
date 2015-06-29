package jalse.entities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation container for {@link EntityID}.
 *
 * @author Elliot Ford
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EntityIDs {

    /**
     * Entity IDs.
     *
     * @return Entity IDs.
     */
    EntityID[]value();
}
