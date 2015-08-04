package jalse.tags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a {@link Tag} marker interface to allow the {@link Taggable} to know that there should
 * only ever be once instance of this tag type.
 *
 * @author Elliot Ford
 *
 * @see Taggable#getSingletonTag(Class)
 * @see TagTypeSet
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SingletonTag {}
