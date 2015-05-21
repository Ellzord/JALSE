package jalse.entities;

import static jalse.entities.Entities.isEntitySubtype;
import jalse.entities.functions.DefaultFunction;
import jalse.entities.functions.EntityFunction;
import jalse.entities.functions.EntityFunctionResolver;
import jalse.entities.functions.EntityMethodFunction;
import jalse.entities.functions.GetAttributeFunction;
import jalse.entities.functions.GetEntitiesFunction;
import jalse.entities.functions.GetEntityFunction;
import jalse.entities.functions.NewEntityFunction;
import jalse.entities.functions.SetAttributeFunction;
import jalse.entities.functions.StreamEntitiesFunction;
import jalse.entities.methods.EntityMethod;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the default {@link EntityProxyFactory} implementation for JALSE. This proxy factory will
 * use {@link EntityFunctionResolver} to resolve an {@link EntityFunction} to translate the proxy
 * {@link Method} calls to {@link EntityMethod}s. Override {@link #addResolverFunctions()} to change
 * what {@link EntityMethodFunction}s are added to the resolver. <br>
 * <br>
 * NOTE: Proxies are cached per {@link Entity} using {@link EntityProxyCache}.
 *
 * @author Elliot Ford
 *
 * @see #uncacheProxyOfEntity(Entity, Class)
 * @see #uncacheProxiesOfEntity(Entity)
 * @see #uncacheAllProxies()
 *
 */
public class DefaultEntityProxyFactory implements EntityProxyFactory {

    /**
     * A {@link WeakHashMap} cache for {@link Entity} proxies.
     *
     * @author Elliot Ford
     *
     * @see Collections#synchronizedMap(Map)
     *
     */
    protected class EntityProxyCache {

	private final Map<Entity, Map<Class<?>, Object>> proxyMap;

	/**
	 * Creates a new entity proxy cache.
	 */
	private EntityProxyCache() {
	    proxyMap = Collections.synchronizedMap(new WeakHashMap<>());
	}

	/**
	 * Either retrieves a proxy from the cache or creates (and caches) a new entity proxy.
	 *
	 * @param e
	 *            Entity to cache type for.
	 * @param type
	 *            Entity type to proxy.
	 * @return Proxy entity of type.
	 *
	 * @see EntityProxyHandler
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entity> T getOrNew(final Entity e, final Class<T> type) {
	    final Map<Class<?>, Object> proxies = proxyMap.computeIfAbsent(e, k -> new ConcurrentHashMap<>());
	    return (T) proxies.computeIfAbsent(type, k -> newEntityProxy(e, k));
	}

	/**
	 * Uncaches all proxies.
	 */
	public void invalidateAll() {
	    proxyMap.clear();
	}

	/**
	 * Uncaches all proxies for an entity.
	 *
	 * @param e
	 *            Entity key.
	 */
	public void invalidateEntity(final Entity e) {
	    proxyMap.remove(e);
	}

	/**
	 * Uncaches a the proxy of the specified type for the entity.
	 *
	 * @param e
	 *            Entity key.
	 * @param type
	 *            Entity type to remove.
	 */
	public void invalidateType(final Entity e, final Class<? extends Entity> type) {
	    final Map<Class<?>, Object> proxies = proxyMap.get(e);
	    if (proxies != null) {
		proxies.remove(type);
		proxyMap.computeIfPresent(e, (k, v) -> v.isEmpty() ? null : v);
	    }
	}

	private Object newEntityProxy(final Entity e, final Class<?> type) {
	    return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },
		    new EntityProxyHandler(e));
	}
    }

    /**
     * The {@link InvocationHandler} for the entity proxies. The {@link Entity} is wrapped in a
     * {@link WeakReference}.
     *
     * @author Elliot Ford
     *
     */
    protected class EntityProxyHandler implements InvocationHandler {

	private final WeakReference<Entity> entityRef;

	/**
	 * Creates a new entity proxy handler.
	 *
	 * @param entity
	 *            Entity to proxy events for.
	 */
	private EntityProxyHandler(final Entity entity) {
	    entityRef = new WeakReference<Entity>(entity);
	}

	/**
	 * Gets the entity this proxy is for.
	 *
	 * @return Host entity.
	 *
	 * @see WeakReference
	 */
	public Entity getEntity() {
	    final Entity entity = entityRef.get();
	    if (entity == null) {
		throw new IllegalStateException("Entity reference lost");
	    }
	    return entity;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	    /*
	     * Get Entity reference.
	     */
	    final Entity entity = getEntity();

	    /*
	     * Not an Entity subclass.
	     */
	    final Class<?> declaringClazz = method.getDeclaringClass();
	    if (!isEntitySubtype(declaringClazz)) {
		return method.invoke(entity, args);
	    }

	    /*
	     * Invoke resolved method.
	     */
	    @SuppressWarnings("unchecked")
	    final EntityFunction resolvedType = resolver.resolveType((Class<? extends Entity>) declaringClazz);
	    final EntityMethod entityMethod = resolvedType.apply(method);
	    return entityMethod.invoke(proxy, entity, args);
	}
    }

    /**
     * Resolver for entity types.
     */
    protected final EntityFunctionResolver resolver;

    /**
     * Proxy cache for entities.
     */
    protected final EntityProxyCache cache;

    /**
     * Creates a new DefaultEntityProxyFactory.
     */
    public DefaultEntityProxyFactory() {
	resolver = new EntityFunctionResolver();
	cache = new EntityProxyCache();
	addResolverFunctions();
    }

    /**
     * Adds the {@link EntityMethodFunction}s to the resolver.
     *
     * @see DefaultFunction
     * @see GetAttributeFunction
     * @see SetAttributeFunction
     * @see NewEntityFunction
     * @See GetEntityFunction
     * @see StreamEntitiesFunction
     * @see GetEntitiesFunction
     */
    protected void addResolverFunctions() {
	resolver.addMethodFunction(new DefaultFunction());
	resolver.addMethodFunction(new GetAttributeFunction());
	resolver.addMethodFunction(new SetAttributeFunction());
	resolver.addMethodFunction(new NewEntityFunction());
	resolver.addMethodFunction(new GetEntityFunction());
	resolver.addMethodFunction(new StreamEntitiesFunction());
	resolver.addMethodFunction(new GetEntitiesFunction());
    }

    @Override
    public boolean isProxyEntity(final Entity e) {
	return Proxy.isProxyClass(e.getClass()) && Proxy.getInvocationHandler(e) instanceof EntityProxyHandler;
    }

    @Override
    public <T extends Entity> T proxyOfEntity(final Entity e, final Class<T> type) {
	validateType(type);
	return cache.getOrNew(e, type);
    }

    /**
     * Uncaches all proxies.
     */
    public void uncacheAllProxies() {
	resolver.unresolveAllTypes();
	cache.invalidateAll();
    }

    /**
     * Uncaches all proxies for an entity.
     *
     * @param e
     *            Entity to uncache for.
     */
    public void uncacheProxiesOfEntity(final Entity e) {
	cache.invalidateEntity(e);
    }

    /**
     * Uncaches the specific type proxy for an entity.
     *
     * @param e
     *            Entity to uncache for.
     * @param type
     *            Proxy type.
     */
    public void uncacheProxyOfEntity(final Entity e, final Class<? extends Entity> type) {
	cache.invalidateType(e, type);
    }

    @Override
    public void validateType(final Class<? extends Entity> type) {
	resolver.resolveType(type);
    }
}
