package jalse.entities.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import jalse.attributes.AttributeContainer;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.methods.NewEntityMethod;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link EntityContainer#newEntity()}</li>
 * <li>{@link EntityContainer#newEntity(UUID)}</li>
 * <li>{@link EntityContainer#newEntity(Class)}</li>
 * <li>{@link EntityContainer#newEntity(AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, Class)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(Class, AttributeContainer)}</li>
 * <li>{@link EntityContainer#newEntity(UUID, Class, AttributeContainer)}</li>
 * </ul>
 * See {@link NewEntityMethod} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see NewEntityMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NewEntity {}
