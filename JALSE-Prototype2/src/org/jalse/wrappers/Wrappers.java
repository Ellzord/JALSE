package org.jalse.wrappers;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jalse.attributes.Attribute;
import org.jalse.misc.JALSEException;

public final class Wrappers {

    private static Set<Method> CACHED_METHODS = Collections.unmodifiableSet(new HashSet<Method>() {

	private static final long serialVersionUID = -3273614078225830902L;

	{
	    addAll(Arrays.asList(AgentWrapper.class.getMethods()));
	    addAll(Arrays.asList(Object.class.getMethods()));
	}
    });

    private static Set<Class<?>> VALID_WRAPPERS = new CopyOnWriteArraySet<Class<?>>() {

	private static final long serialVersionUID = -3273614078225830902L;

	{
	    add(AgentWrapper.class);
	}
    };

    private static void validateWrapper(final Class<?> clazz) {

	if (!AgentWrapper.class.isAssignableFrom(clazz)) {

	    throw JALSEException.INVALID_AGENT_WRAPPER.get();
	}

	if (!VALID_WRAPPERS.contains(clazz)) {

	    for (final Method m : clazz.getDeclaredMethods()) {

		boolean valid = false;

		final Class<?>[] args = m.getParameterTypes();
		final Type rt = m.getGenericReturnType();
		Class<?> attr = null;

		if (rt instanceof ParameterizedType) {

		    final ParameterizedType grt = (ParameterizedType) rt;

		    if (Optional.class.equals(grt.getRawType())) {

			attr = (Class<?>) grt.getActualTypeArguments()[0];
		    }
		}

		if (args.length == 0) {

		    valid = attr != null && Attribute.class.isAssignableFrom(attr);
		}
		else {

		    valid = args.length == 1;
		    valid = valid && Attribute.class.isAssignableFrom(args[0]);

		    if (valid && !Void.TYPE.equals(rt)) {

			valid = valid && attr != null && Attribute.class.isAssignableFrom(attr);
			valid = valid && attr.equals(args[0]);
		    }
		}

		if (!valid) {

		    throw JALSEException.INVALID_AGENT_WRAPPER.get();
		}
	    }

	    VALID_WRAPPERS.add(clazz);

	    for (final Class<?> iter : clazz.getInterfaces()) {

		validateWrapper(iter);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public static <T extends AgentWrapper> T wrap(final AgentWrapper agent, final Class<T> clazz) {

	validateWrapper(clazz);

	return (T) Proxy.newProxyInstance(
		clazz.getClassLoader(),
		new Class[] { clazz },
		(p, m, a) -> {

		    Object result = null;

		    if (CACHED_METHODS.contains(m)) {

			result = m.invoke(agent, a);
		    }
		    else {

			final Class<? extends Attribute> attr = (Class<? extends Attribute>) ((ParameterizedType) m
				.getGenericReturnType()).getActualTypeArguments()[0];

			if (a == null || a.length == 0) {

			    result = agent.getAttribute(attr);
			}
			else {

			    result = a[0] != null ? agent.associate((Attribute) a[0]) : agent.disassociate(attr);
			}
		    }

		    return result;
		});
    }

    private Wrappers() {

	throw new UnsupportedOperationException();
    }
}
