package jalse.entities.functions;

import jalse.entities.Entity;
import jalse.entities.methods.EntityMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * This is a resolver for {@link Entity} types. It can be given a number of
 * {@link EntityMethodFunction} that can be used to process {@link Method}s from an {@link Entity}
 * type and then map them to an {@link EntityMethod}. Once all methods are resolved a
 * {@link EntityFunction} can be obtained for the original {@link Entity} type (so an
 * {@link InvocationHandler} can use the mapped methods). <br>
 * <br>
 * The resolving process can throw exceptions if it does not resolve correctly. Types resolved (
 * {@link #resolveType(Class)}) are cached so they do not have to be resolved again (unless
 * {@link #unresolveType(Class)} or {@link #unresolveAllTypes()} are called). <br>
 * <br>
 * NOTE: this is a thread-safe implementation.
 *
 * @author Elliot Ford
 *
 */
public class EntityFunctionResolver {

    private static final Logger logger = Logger.getLogger(EntityFunctionResolver.class.getName());

    private final Map<Class<? extends Entity>, EntityFunction> resolved;
    private final Set<EntityMethodFunction> functions;

    /**
     * Creates a new type resolver (no method functions).
     */
    public EntityFunctionResolver() {
	resolved = new ConcurrentHashMap<>();
	functions = new CopyOnWriteArraySet<>();
    }

    /**
     * Creates a new type resolver with the supplied method functions.
     *
     * @param methodFunctions
     *            Method functions to add.
     */
    public EntityFunctionResolver(final Set<EntityMethodFunction> methodFunctions) {
	this();
	addMethodFunctions(methodFunctions);
    }

    /**
     * Adds a method function.
     *
     * @param methodFunction
     *            Function to add.
     * @return Whether the function was not already added.
     */
    public boolean addMethodFunction(final EntityMethodFunction methodFunction) {
	return functions.add(methodFunction);
    }

    /**
     * Adds a number of method functions.
     *
     * @param methodFunctions
     *            Functions to add.
     */
    public void addMethodFunctions(final Set<? extends EntityMethodFunction> methodFunctions) {
	functions.addAll(methodFunctions);
    }

    /**
     * Gets all of the resolved {@link Entity} types.
     *
     * @return All resolved types.
     */
    public Set<Class<? extends Entity>> getResolvedTypes() {
	return new HashSet<>(resolved.keySet());
    }

    /**
     * Removes all method functions.
     */
    public void removeAllMethodFunctions() {
	functions.clear();
    }

    /**
     * Removes a method function.
     *
     * @param methodFunction
     *            function to remove.
     * @return Whether the function was there to remove.
     */
    public boolean removeMethodResolver(final EntityMethodFunction methodFunction) {
	return functions.remove(methodFunction);
    }

    /**
     * Removes a number of method functions.
     *
     * @param methodFunctions
     *            Functions to remove.
     */
    public void removeMethodResolvers(final Set<? extends EntityMethodFunction> methodFunctions) {
	functions.removeAll(methodFunctions);
    }

    /**
     * Resolves an {@link Entity} type to a new resolved {@link EntityFunction}.
     *
     * @param type
     *            Type to resolve.
     * @return New (or cached) resolved mapped type.
     */
    @SuppressWarnings("unchecked")
    public EntityFunction resolveType(final Class<? extends Entity> type) {
	// Check already resolved
	EntityFunction resolvedType = resolved.get(Objects.requireNonNull(type));
	if (resolvedType != null) {
	    return resolvedType;
	}

	// Log type
	logger.info(String.format("Resolving type %s", type));

	// Check not entity
	if (Entity.class.equals(type)) {
	    throw new IllegalArgumentException("Entity is not a valid Entity subclass");
	}

	// Check is interface
	if (!type.isInterface()) {
	    throw new IllegalArgumentException(String.format("Type %s is not an interface", type));
	}

	// Check type parameters
	if (type.getTypeParameters().length > 0) {
	    throw new IllegalArgumentException(String.format("Type %s cannot take type parameters", type));
	}

	// Build method map
	final Map<Method, EntityMethod> methodMap = new HashMap<>();
	final Set<Class<? extends Entity>> totalDependencies = new HashSet<>();
	for (final Method m : type.getDeclaredMethods()) {
	    // Check static
	    if (Modifier.isStatic(m.getModifiers())) {
		// Ignore static methods as not invoked on instance
		continue;
	    }
	    // Process functions
	    for (final EntityMethodFunction methodFunction : functions) {
		// Log method
		logger.fine(String.format("Resolving method %s with function %s", m, methodFunction.getClass()));
		// Resolve method
		final EntityMethod em = methodFunction.apply(m);
		if (em != null) {
		    // Resolved method
		    if (methodMap.put(m, em) != null) {
			throw new IllegalArgumentException(String.format(
				"Method %s of type %s had multiple function hits", m, type));
		    }
		    // Add dependencies to be resolved
		    totalDependencies.addAll(em.getDependencies());
		}
	    }
	    // Check method could be resolved
	    if (!methodMap.containsKey(m)) {
		throw new IllegalArgumentException(String.format("Method %s of type %s had no resolver hits", m, type));
	    }
	}

	// Created resolved type
	resolvedType = new EntityFunction(type, methodMap);
	resolved.put(type, resolvedType);

	// Resolve dependencies
	totalDependencies.forEach(this::resolveType);

	// Resolve parents
	for (final Class<?> parent : type.getInterfaces()) {
	    // Ignore if entity
	    if (Entity.class.equals(parent)) {
		continue;
	    }

	    // Check if entity subclass
	    if (!Entity.class.isAssignableFrom(parent)) {
		throw new IllegalArgumentException(String.format("Parent type %s of type %s is not an Entity", parent,
			type));
	    }

	    // Resolve dependency
	    resolveType((Class<? extends Entity>) parent);
	}

	return resolvedType;
    }

    /**
     * Uncaches all resolved types.
     */
    public void unresolveAllTypes() {
	resolved.clear();
    }

    /**
     * Uncaches a resolved type.
     *
     * @param type
     *            Type to uncache.
     *
     * @return Whether the type was present in the resolver.
     */
    public boolean unresolveType(final Class<? extends Entity> type) {
	return resolved.remove(Objects.requireNonNull(type)) != null;
    }
}
