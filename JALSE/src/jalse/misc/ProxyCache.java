package jalse.misc;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * A type validating proxy cache where proxies of different types can be associated to an owner. The
 * cache uses {@link WeakReference} keys so when there are no references to the owner the proxies
 * are dropped. This cache works on the ideal that each owner will only need one proxy of each type
 * that can be referenced (or created if it does not yet exist - {@link #getOrNew(Object, Class)} ).<br>
 * <br>
 * The proxies that are created and the types that are valid are defined controlled by
 * {@link ProxyFactory}. This class should make {@link CacheHandler} instances to be used as
 * {@link InvocationHandler} for the newly created proxy instances.<br>
 * <br>
 * ProxyCache will validate types based on {@link ProxyFactory#validate(Class)} and this can be done
 * by {@link #validateType(Class)} or the type will be validated when a proxy is accessed.
 * ProxyCache keeps track of validated types so will not re-validate unless
 * {@link #invalidateType(Class)} is called (this also drops the proxies matching this type from the
 * cache).
 *
 * @author Elliot Ford
 *
 */
public class ProxyCache {

    /**
     * An abstract {@link InvocationHandler} implementation that references a {@link WeakReference}
     * owner. This should be extended with the logic needed for the proxy.
     *
     * @author Elliot Ford
     *
     */
    public static abstract class CacheHandler implements InvocationHandler {

	private final WeakReference<Object> obj;
	private final Class<?> proxyClazz;

	/**
	 * Creates a new instance of CacheHandler.
	 *
	 * @param obj
	 *            The owner.
	 * @param proxyClazz
	 *            Proxy type.
	 */
	protected CacheHandler(final Object obj, final Class<?> proxyClazz) {
	    this.obj = new WeakReference<>(Objects.requireNonNull(obj));
	    this.proxyClazz = Objects.requireNonNull(proxyClazz);
	}

	/**
	 * Gets the proxy type.
	 *
	 * @return Proxy type.
	 */
	public Class<?> getForClass() {
	    return proxyClazz;
	}

	/**
	 * Gets the owner.
	 *
	 * @return The owner.
	 */
	public Object getOwner() {
	    return obj.get();
	}

	/**
	 * Checks whether this handler can be used in stead of another proxy type.
	 *
	 * @param clazz
	 *            Possible subclass.
	 * @return Whether the supplied class matches this class or this is a subclass.
	 */
	public boolean isOrSubclass(final Class<?> clazz) {
	    return clazz.isAssignableFrom(proxyClazz);
	}
    }

    /**
     * Proxy type validator and {@link CacheHandler} creation factory. Validation of each class will
     * only be called once (unless invalidated).
     *
     * @author Elliot Ford
     *
     */
    public interface ProxyFactory {

	/**
	 * Creates a CacheHandler instance using the supplied owner and proxy type.
	 *
	 * @param obj
	 *            Owner.
	 * @param type
	 *            ProxyType.
	 * @return Newly created invoation handler.
	 */
	CacheHandler newHandler(Object obj, Class<?> type);

	/**
	 * Validates the class suitable for the cache.
	 *
	 * @param type
	 *            Type to validate.
	 * @return Whether the class is valid.
	 */
	boolean validate(Class<?> type);
    }

    private final ProxyFactory factory;
    private final Map<Object, Map<Class<?>, Object>> proxies;
    private final Set<Class<?>> validTypes;

    private final StampedLock lock;

    /**
     * Creates a new instance of ProxyCache with the supplied factory.
     *
     * @param factory
     *            Proxy Factory.
     */
    public ProxyCache(final ProxyFactory factory) {
	this.factory = Objects.requireNonNull(factory);
	proxies = new WeakHashMap<>();
	validTypes = new HashSet<>();
	lock = new StampedLock();
    }

    private long checkValidateType(final long stamp, final Class<?> type) {
	if (validTypes.contains(type)) { // Already valid
	    return stamp;
	}

	final long ws = lock.tryConvertToWriteLock(stamp);
	long newStamp;

	if (ws != 0L) { // Could not convert
	    newStamp = ws;
	} else {
	    lock.unlockRead(stamp);
	    newStamp = lock.writeLock();
	    if (validTypes.contains(type)) { // Re-check may have lost lock
		return newStamp;
	    }
	}

	if (factory.validate(type)) {
	    validTypes.add(type);
	}

	return newStamp;
    }

    private long ensureEntryForObj(final Object obj, final long stamp, final boolean forceWrite) {
	long newStamp = stamp;

	if (lock.isReadLocked()) {
	    if (forceWrite || !proxies.containsKey(obj)) {
		final long ws = lock.tryConvertToWriteLock(stamp);
		if (ws != 0L) { // Could not convert
		    newStamp = ws;
		} else {
		    lock.unlockRead(stamp);
		    newStamp = lock.writeLock();
		}
	    }
	}

	if (!proxies.containsKey(obj)) { // Re-check may have lost lock
	    proxies.put(obj, new HashMap<>());
	}

	return newStamp;
    }

    /**
     * Gets the proxy factory.
     *
     * @return Proxy factory.
     */
    public ProxyFactory getFactory() {
	return factory;
    }

    /**
     * Gets (if possible) or creates a new proxy for the supplied owner and proxy type. If the
     * supplied owner is another proxy from ProxyCache then if the supplied type is a super class of
     * the proxy type the proxy type can be used.
     *
     * @param obj
     *            Owner object.
     * @param type
     *            proxy type.
     * @return Existing or newly created proxy.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrNew(final Object obj, final Class<T> type) {
	Object unwrapped = obj;

	if (Proxy.isProxyClass(obj.getClass())) {
	    final InvocationHandler invocHandler = Proxy.getInvocationHandler(obj);
	    if (invocHandler instanceof CacheHandler) {
		final CacheHandler cacheHandler = (CacheHandler) invocHandler;
		if (cacheHandler.isOrSubclass(type)) { // Don't make proxies of our proxies
		    return (T) obj;
		}

		unwrapped = cacheHandler.getOwner();
	    }
	}

	long stamp = lock.readLock();
	try {
	    stamp = checkValidateType(stamp, type); // Validate and keep lock
	    if (!validTypes.contains(type)) { // Failed
		throw new IllegalArgumentException("Supplied type did not pass validation");
	    }

	    stamp = ensureEntryForObj(unwrapped, stamp, false); // Ensure can get objProxies
	    Map<Class<?>, Object> objProxies = proxies.get(unwrapped);

	    Object proxy = objProxies.get(type);
	    if (proxy != null) { // Read-only already exists
		return (T) proxy;
	    } else { // Entry doesn't exist (maybe write lock).
		stamp = ensureEntryForObj(unwrapped, stamp, true);
		objProxies = proxies.get(unwrapped);
		proxy = objProxies.get(type);

		if (proxy != null) { // May have lost lock
		    return (T) proxy;
		}
	    }

	    /*
	     * New proxy handler of object.
	     */
	    proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },
		    factory.newHandler(unwrapped, type));
	    objProxies.put(type, proxy);

	    return (T) proxy;
	} finally {
	    lock.unlock(stamp);
	}
    }

    /**
     * Gets the total proxy count.
     *
     * @return Total count.
     */
    public int getProxyCount() {
	final long stamp = lock.readLock();
	try {
	    return proxies.values().stream().mapToInt(Map::size).sum();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets the proxy count for the supplied owner.
     *
     * @param obj
     *            Owner object.
     * @return Proxy count.
     */
    public int getProxyCount(final Object obj) {
	final long stamp = lock.readLock();
	try {
	    final Map<Class<?>, Object> objProxies = proxies.get(obj);
	    return objProxies != null ? objProxies.size() : 0;
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets the total valid types within the cache.
     *
     * @return All valid types.
     */
    public Set<Class<?>> getValidTypes() {
	final long stamp = lock.readLock();
	try {
	    return new HashSet<>(validTypes);
	} finally {
	    lock.unlock(stamp);
	}
    }

    /**
     * Whether the cache has proxies.
     *
     * @return Proxy count > 0.
     */
    public boolean hasProxies() {
	return getProxyCount() > 0;
    }

    /**
     * Whether the supplied owner has proxies.
     *
     * @param obj
     *            Owner object.
     * @return Owner proxy count > 0.
     */
    public boolean hasProxies(final Object obj) {
	return getProxyCount(obj) > 0;
    }

    /**
     * Whether the owner has a proxy of matching the supplied type.
     *
     * @param obj
     *            Owner object.
     * @param type
     *            Proxy type.
     * @return Whether the owner has a proxy of matching type.
     */
    public boolean hasProxy(final Object obj, final Class<?> type) {
	final long stamp = lock.readLock();
	try {
	    final Map<Class<?>, Object> objProxies = proxies.get(obj);
	    return objProxies != null ? objProxies.containsKey(type) : false;
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Invalids the supplied type (this will clear matching proxies).
     *
     * @param type
     *            Proxy type to invalidate.
     */
    public void invalidateType(final Class<?> type) {
	long stamp = lock.readLock();
	try {
	    if (!validTypes.contains(type)) {
		return;
	    } else {
		final long ws = lock.tryConvertToWriteLock(stamp);
		if (ws != 0L) {
		    stamp = ws;
		} else {
		    lock.unlockRead(stamp);
		    stamp = lock.writeLock();

		    if (!validTypes.contains(type)) { // May have lost lock.
			return;
		    }
		}

		validTypes.remove(type);
		for (final Map<Class<?>, Object> objProxies : proxies.values()) {
		    objProxies.remove(type); // Clear previous proxies.
		}
	    }

	} finally {
	    lock.unlock(stamp);
	}
    }

    /**
     * Checks whether the supplied type has been validated successfully.
     *
     * @param type
     *            Proxy type.
     * @return Whether the proxy type has been validated successfully.
     *
     */
    public boolean isValidatedType(final Class<?> type) {
	final long stamp = lock.readLock();
	try {
	    return validTypes.contains(type);
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Removes a proxy from the cache for the supplied owner and matching the supplied type.
     *
     * @param obj
     *            Owner object.
     * @param type
     *            Proxy type.
     */
    public void remove(final Object obj, final Class<?> type) {
	long stamp = lock.readLock();
	try {
	    Map<Class<?>, Object> objProxies = proxies.get(obj);
	    if (objProxies == null) {
		return;
	    } else if (objProxies.containsKey(type)) {
		final long ws = lock.tryConvertToWriteLock(stamp);
		if (ws != 0L) {
		    stamp = ws;
		} else {
		    lock.unlockRead(stamp);
		    stamp = lock.writeLock();
		    objProxies = proxies.get(obj);

		    if (objProxies == null || !objProxies.containsKey(type)) { // May have lost
			// lock.
			return;
		    }
		}
		objProxies.remove(type);
	    }
	} finally {
	    lock.unlock(stamp);
	}
    }

    /**
     * Removes all proxies.
     */
    public void removeAll() {
	final long stamp = lock.writeLock();
	try {
	    proxies.clear();
	} finally {
	    lock.unlockWrite(stamp);
	}
    }

    /**
     * Removes all proxies for the supplied owner.
     *
     * @param obj
     *            Owner object.
     */
    public void removeAll(final Object obj) {
	long stamp = lock.readLock();
	try {
	    final boolean hasProxies = proxies.containsKey(obj);
	    if (hasProxies) {
		final long ws = lock.tryConvertToWriteLock(stamp);
		if (ws != 0L) {
		    stamp = ws;
		} else {
		    lock.unlockRead(stamp);
		    stamp = lock.writeLock();
		    if (!proxies.containsKey(obj)) { // May have lost lock.
			return;
		    }
		}
		proxies.remove(obj);
	    }
	} finally {
	    lock.unlock(stamp);
	}
    }

    /**
     * Force validates a proxy type.
     *
     * @param type
     *            Proxy type to validate.
     * @return Whether the type is valid.
     */
    public boolean validateType(final Class<?> type) {
	long stamp = lock.readLock();
	try {
	    stamp = checkValidateType(stamp, type);
	    return validTypes.contains(type);
	} finally {
	    lock.unlock(stamp);
	}
    }
}