package jalse.agents;

import static jalse.misc.JALSEExceptions.INVALID_AGENT_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.attributes.Attribute;
import jalse.misc.JALSEExceptions;

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

/**
 * Agents is a utility for wrapping agents to different agent types. An agent
 * type requires two criteria be met:<br>
 * 1. The type {@code interface} inherits from {@link Agent} as well as so do
 * all of its ancestors.<br>
 * 2. Agent types can only provide get or set style methods (though they do not
 * have to be named as such). Get methods must have a return type of
 * {@link Optional} containing the {@Attribute} type that to
 * retrieve and take no arguments (equivalent to {@link Agent#getOfType(Class)}
 * ). Set methods can have either a void return type or a return type of
 * {@link Optional} containing the {@link Attribute} to be set and take only the
 * attribute as an argument (equivalent to {@link Agent#associate(Attribute)}).
 * When set methods have void return types the replaced value can not be
 * returned when setting a new value. Setting a new value to null will remove
 * the attribute (equivalent to {@link Agent#disassociate(Class)}).<br>
 * <br>
 *
 * An example agent type:
 *
 * <pre>
 * <code>
 * public interface Cow extends Agent {
 *
 * 	Optional{@code<Moo>} getMoo();
 *
 * 	void setMoo(Moo m);
 * }
 * </code>
 *
 * <pre>
 *
 * @author Elliot Ford
 *
 */
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

    /**
     * Checks if the specified type is descendant from the specified ancestor.
     * type.
     *
     * @param descendant
     *            Descendant type.
     * @param ancestor
     *            Ancestor type.
     * @return Whether the descendant is descended from the ancestor type.
     */

    /**
     * Gets all ancestors for the specified descendant type (not including
     * {@link Agent}).
     *
     * @param type
     *            Descendant type.
     * @return All ancestors or an empty set if its only ancestor is
     *         {@link Agent}.
     *
     * @throws IllegalArgumentException
     *             If the agent type is invalid
     *
     * @see JALSEExceptions#INVALID_AGENT_TYPE
     */
    public static Set<Class<? extends Agent>> getAncestry(final Class<? extends Agent> type) {

	validateType(type);

	final Set<Class<? extends Agent>> ancestry = new HashSet<>();

	addAncestors(ancestry, type);

	return ancestry;
    }

    @SuppressWarnings("unchecked")
    private static void addAncestors(final Set<Class<? extends Agent>> ancestry, final Class<?> type) {

	for (final Class<?> t : type.getInterfaces()) {

	    if (!t.equals(Agent.class) && ancestry.add((Class<? extends Agent>) t)) {

		addAncestors(ancestry, t);
	    }
	}
    }

    /**
     * Validates a specified agent type according the criteria defined above.
     * The ancestor {@code interface} {@link Agents} is considered to be
     * invalid.
     *
     * @param type
     *            Agent type to validate.
     * @throws IllegalArgumentException
     *             If the agent type fails validation.
     */
    public static void validateType(final Class<? extends Agent> type) {

	if (type.equals(Agent.class)) {

	    throwRE(INVALID_AGENT_TYPE);
	}

	validateType0(type);
    }

    private static void validateType0(final Class<?> clazz) {

	if (!Agent.class.isAssignableFrom(clazz)) {

	    throwRE(INVALID_AGENT_TYPE);
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

		    throwRE(INVALID_AGENT_TYPE);
		}
	    }

	    VALID_AGENTS.add(clazz);

	    for (final Class<?> iter : clazz.getInterfaces()) {

		validateType0(iter);
	    }
	}
    }

    /**
     * Wraps an agent as the supplied agent type.
     *
     * @param agent
     *            Agent to wrap.
     * @param type
     *            Agent type to wrap to.
     * @return The wrapped agent.
     *
     * @throws NullPointerException
     *             If the agent or agent type are null.
     * @throws IllegalArgumentException
     *             If the agent type does not meet the criteria defined above.
     *
     * @see Agents
     * @see JALSEExceptions#INVALID_AGENT_TYPE
     */
    @SuppressWarnings("unchecked")
    public static <T extends Agent> T asType(final Agent agent, final Class<T> type) {

	validateType(type);

	return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, (p, m, a) -> {

	    Object result = null;

	    if (CACHED_METHODS.contains(m)) {

		result = m.invoke(agent, a);
	    }
	    else {

		final Type rt = m.getGenericReturnType();

		if (a == null || a.length == 0) {

		    result = agent.getOfType(getAttrClass(rt));
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

    /**
     * Checks if the specified type is equal to or a descendant from the
     * specified ancestor. type.
     *
     * @param descendant
     *            Descendant type.
     * @param ancestor
     *            Ancestor type.
     * @return Whether the descendant is equal or descended from the ancestor
     *         type.
     */
    public static boolean isOrDescendant(final Class<? extends Agent> descendant,
	    final Class<? extends Agent> ancestor) {

	return ancestor.isAssignableFrom(descendant);
    }
}
