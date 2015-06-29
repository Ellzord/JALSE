package jalse.entities;

import java.lang.reflect.InvocationHandler;

import jalse.entities.functions.EntityFunctionResolver;

/**
 * This defines a factory for creating {@link Entity} proxies. Making a proxy of an {@link Entity}
 * subclass allows the {@link InvocationHandler} to translate the method calls into {@link Entity}
 * methods. This makes the subclasses more customisable for specific problems.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 * @see EntityFunctionResolver
 *
 */
public interface EntityProxyFactory {

    /**
     * Checks whether the supplied entity is a proxy.
     *
     * @param e
     *            Entity to check.
     * @return Whether the entity was a proxy.
     */
    boolean isProxyEntity(final Entity e);

    /**
     * Creates a proxy of the entity subclass.
     *
     * @param e
     *            Entity to proxy for.
     * @param type
     *            Type to proxy as.
     * @return The proxy.
     */
    <T extends Entity> T proxyOfEntity(Entity e, Class<T> type);

    /**
     * Validates the type can be proxied.
     *
     * @param type
     *            Type to validate.
     */
    void validateType(Class<? extends Entity> type);
}
