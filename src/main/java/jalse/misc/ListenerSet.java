package jalse.misc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides a thread-safe way to store and process listeners. Listener set takes in the defining
 * listener {@code interface} and allows you to invoke the defined method upon the whole group via a
 * {@link ListenerSet#getProxy()}.
 *
 * @author Elliot Ford
 *
 * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
 *
 * @param <T>
 *            Listener {@code interface}.
 */
public class ListenerSet<T> extends HashSet<T> implements InvocationHandler {

    private static final long serialVersionUID = 1437345792255852480L;

    private final T proxy;

    /**
     * Creates a new instance of listener set for the supplied listener type.
     *
     * @param clazz
     *            Listener type to store.
     */
    public ListenerSet(final Class<? super T> clazz) {
	this(clazz, null);
    }

    /**
     * Creates a new instance of listener set for the supplied listener type.
     *
     * @param clazz
     *            Listener type to store.
     * @param listeners
     *            Starting listeners.
     */
    @SuppressWarnings("unchecked")
    public ListenerSet(final Class<? super T> clazz, final Set<? extends T> listeners) {
	proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz }, this);
	if (listeners != null) {
	    addAll(listeners);
	}
    }

    @Override
    public boolean add(final T e) {
	return super.add(Objects.requireNonNull(e));
    }

    /**
     * Gets the group proxy for easy invocation of methods upon the group.
     *
     * @return Listener group proxy.
     */
    public T getProxy() {
	return proxy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	for (final T t : this) {
	    method.invoke(t, args);
	}
	return null;
    }

    @Override
    public boolean remove(final Object o) {
	return super.remove(Objects.requireNonNull(o));
    }
}
