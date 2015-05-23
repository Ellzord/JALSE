package jalse.entities.annotations;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.functions.KillEntitiesFunction;
import jalse.entities.methods.KillEntitiesMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link Entity} type annotation for {@link EntityContainer#killEntities()}.<br>
 * <br>
 * See {@link KillEntitiesFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see KillEntitiesMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KillEntities {}
