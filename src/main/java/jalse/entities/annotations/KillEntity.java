package jalse.entities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.functions.KillEntityFunction;
import jalse.entities.methods.KillEntityMethod;

/**
 * An {@link Entity} type annotation for {@link EntityContainer#killEntity(UUID)}.<br>
 * <br>
 * See {@link KillEntityFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see KillEntityMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KillEntity {}
