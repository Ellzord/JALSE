package jalse.entities.methods;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

import jalse.entities.Entity;
import jalse.entities.functions.DefaultFunction;

/**
 * This is used for mapping {@code default} methods.<br>
 * <br>
 * It is worth noting that this class has been designed with performance in mind. Default method
 * invocation on a dynamic proxy is quite problematic and slow (for now).
 *
 * @author Elliot Ford
 *
 * @see DefaultFunction
 */
public class DefaultMethod implements EntityMethod {

    private final MethodHandle handle;
    private final int argCount;

    /**
     * Creates a new default method.
     *
     * @param handle
     *            Method handle to invoke.
     * @param argCount
     *            Method parameter count.
     */
    public DefaultMethod(final MethodHandle handle, final int argCount) {
	this.handle = Objects.requireNonNull(handle);
	if (argCount < 0) {
	    throw new IllegalArgumentException();
	}
	this.argCount = argCount;
    }

    /**
     * Gets the method argument count.
     *
     * @return Argument count.
     */
    public int getArgCount() {
	return argCount;
    }

    /**
     * Gets the method handle to invoke.
     *
     * @return Method handle.
     */
    public MethodHandle getHandle() {
	return handle;
    }

    @Override
    public Object invoke(final Object proxy, final Entity e, final Object[] args) throws Throwable {
	/*
	 * Generally the maximum accepted parameter count for a method is 7.
	 */
	switch (argCount) {
	case 0:
	    return handle.invoke(proxy);
	case 1:
	    return handle.invoke(proxy, args[0]);
	case 2:
	    return handle.invoke(proxy, args[0], args[1]);
	case 3:
	    return handle.invoke(proxy, args[0], args[1], args[2]);
	case 4:
	    return handle.invoke(proxy, args[0], args[1], args[2], args[3]);
	case 5:
	    return handle.invoke(proxy, args[0], args[1], args[2], args[3], args[4]);
	case 6:
	    return handle.invoke(proxy, args[0], args[1], args[2], args[3], args[4], args[5]);
	case 7:
	    return handle.invoke(proxy, args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
	default:
	    /*
	     * This has a huge performance hit!
	     */
	    return handle.bindTo(proxy).invokeWithArguments(args);
	}
    }

}
