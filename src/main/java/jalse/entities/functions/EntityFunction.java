package jalse.entities.functions;

import jalse.entities.Entity;
import jalse.entities.methods.EntityMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A function for a specific {@link Entity} type. This can beused by an {@link InvocationHandler} to
 * translate original method calls to the desired {@link EntityMethod}.
 *
 * @author Elliot Ford
 *
 * @see EntityFunctionResolver
 *
 */
public class EntityFunction implements Function<Method, EntityMethod> {

    private final Class<? extends Entity> type;
    private final Map<Method, EntityMethod> methodMap;

    /**
     * Creates a new entity function with the supplied method mapping.
     *
     * @param type
     *            Entity type this is for.
     * @param methodMap
     *            Method to entity method mapping.
     */
    public EntityFunction(final Class<? extends Entity> type, final Map<Method, EntityMethod> methodMap) {
	this.type = Objects.requireNonNull(type);
	this.methodMap = new ConcurrentHashMap<>(methodMap);
    }

    @Override
    public EntityMethod apply(final Method m) {
	final EntityMethod em = methodMap.get(m);
	if (em == null) {
	    throw new IllegalArgumentException(String.format("Could not resolve method %s", m));
	}
	return em;
    }

    @Override
    public final boolean equals(final Object obj) {
	return obj == this || obj instanceof EntityFunction && type.equals(((EntityFunction) obj).type);
    }

    /**
     * Gets the entity type this is for.
     *
     * @return Entity type.
     */
    public Class<? extends Entity> getType() {
	return type;
    }

    @Override
    public final int hashCode() {
	return 31 * 1 + type.hashCode();
    }
}
