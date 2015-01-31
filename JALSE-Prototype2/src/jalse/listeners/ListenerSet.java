package jalse.listeners;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

public class ListenerSet<T> extends CopyOnWriteArraySet<T> implements InvocationHandler {

    private static final long serialVersionUID = 1437345792255852480L;

    private final T proxy;

    @SuppressWarnings("unchecked")
    public ListenerSet(final Class<T> clazz) {

	proxy = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
    }

    public T getProxy() {

	return proxy;
    }

    @Override
    public boolean add(final T e) {

	return super.add(Objects.requireNonNull(e));
    }

    @Override
    public boolean remove(final Object o) {

	return super.remove(Objects.requireNonNull(o));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

	for (final T t : this) {

	    method.invoke(t, args);
	}

	return null;
    }
}
