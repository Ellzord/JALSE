package jalse.listeners;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Provides a thread-safe way to store and process listeners. Listener set takes in the defining
 * listener {@code interface} and allows you to invoke the defined method upon the whole group via a
 * proxy {@link ListenerSet#getProxy()}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Listener {@code interface}.
 */
public class ListenerSet<T> extends CopyOnWriteArraySet<T> implements InvocationHandler {

    private static final long serialVersionUID = 1437345792255852480L;

    private final T proxy;

    /**
     * Creates a new instance of listener set for the supplied listener type.
     *
     * @param clazz
     *            Listener type to store.
     */
    @SuppressWarnings("unchecked")
    public ListenerSet(final Class<T> clazz) {
	proxy = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
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
