package jalse.entities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.functions.GetEntityFunction;
import jalse.entities.methods.GetEntityMethod;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link EntityContainer#getEntityAsType(UUID, Class)}</li>
 * <li>{@link EntityContainer#getOptEntityAsType(UUID, Class)}</li>
 * </ul>
 * See {@link GetEntityFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see GetEntityMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetEntity {}
