package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * An {@link Entity} type annotation for {@link EntityContainer#getEntityAsType(UUID, Class)}.
 *
 * @author Elliot Ford
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetEntity {}
