package jalse.agents;

import static jalse.misc.JALSEExceptions.INVALID_AGENT;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.attributes.Attribute;

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

public final class Agents {

    private static Set<Method> CACHED_METHODS = Collections.unmodifiableSet(new HashSet<Method>() {

	private static final long serialVersionUID = -3273614078225830902L;

	{
	    addAll(Arrays.asList(Agent.class.getMethods()));
	    addAll(Arrays.asList(Object.class.getMethods()));
	}
    });

    private static Set<Class<?>> VALID_AGENTS = new CopyOnWriteArraySet<Class<?>>() {

	private static final long serialVersionUID = -3273614078225830902L;

	{
	    add(Agent.class);
	}
    };

    @SuppressWarnings("unchecked")
    private static Class<? extends Attribute> getAttrClass(final Type type) {

	return (Class<? extends Attribute>) ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private static void validateAgent(final Class<?> clazz) {

	if (!Agent.class.isAssignableFrom(clazz)) {

	    throwRE(INVALID_AGENT);
	}

	if (!VALID_AGENTS.contains(clazz)) {

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

		    throwRE(INVALID_AGENT);
		}
	    }

	    VALID_AGENTS.add(clazz);

	    for (final Class<?> iter : clazz.getInterfaces()) {

		validateAgent(iter);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    public static <T extends Agent> T wrap(final Agent agent, final Class<T> clazz) {

	validateAgent(clazz);

	return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, (p, m, a) -> {

	    Object result = null;

	    if (CACHED_METHODS.contains(m)) {

		result = m.invoke(agent, a);
	    }
	    else {

		final Type rt = m.getGenericReturnType();

		if (a == null || a.length == 0) {

		    result = agent.getAttribute(getAttrClass(rt));
		}
		else {

		    result = a[0] != null ? agent.associate((Attribute) a[0]) : agent.disassociate(getAttrClass(rt));
		}
	    }

	    return result;
	});
    }

    private Agents() {

	throw new UnsupportedOperationException();
    }
}
