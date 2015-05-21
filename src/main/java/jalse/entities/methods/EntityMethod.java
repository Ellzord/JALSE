package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.functions.EntityFunctionResolver;
import jalse.entities.functions.EntityMethodFunction;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * This is used to map from a {@link Method} called on an {@link Entity} type to a desired function.
 * An {@link EntityMethodFunction} will resolve the correct implemntation of this class depending on
 * the method signature (or annotations). This is used to build an {@link Entity} type using
 * {@link EntityFunctionResolver}.
 *
 * @author Elliot Ford
 *
 */
@FunctionalInterface
public interface EntityMethod {

    /**
     * Optional referenced entity dependencies (empty by default).
     *
     * @return Referenced entities.
     */
    default Set<Class<? extends Entity>> getDependencies() {
	return Collections.emptySet();
    }

    /**
     * This will invoke the desired functionality for this method using the proxy, entity and
     * supplied arguments.
     *
     * @param proxy
     *            Possible proxy of entity type.
     * @param entity
     *            Entity this proxy is for.
     * @param args
     *            Supplied arguments to the original method.
     * @return Invocation result.
     * @throws Throwable
     *             If the invocation caused an exception.
     */
    Object invoke(Object proxy, Entity entity, Object[] args) throws Throwable;
}
