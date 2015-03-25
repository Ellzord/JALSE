package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * An {@link Entity} type annotation for {@link EntityContainer#newEntity(Class)} and
 * {@link EntityContainer#newEntity(UUID, Class)}.<br>
 * <br>
 * The method definition will be used to select the correct method (UUID parameter).
 *
 * @author Elliot Ford
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NewEntity {}
