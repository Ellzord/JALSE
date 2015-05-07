package jalse.entities;

import static jalse.misc.JALSEExceptions.INVALID_ENTITY_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.attributes.AttributeContainer;
import jalse.attributes.Attributes;
import jalse.attributes.NamedAttributeType;
import jalse.entities.annotations.GetAttribute;
import jalse.entities.annotations.GetEntities;
import jalse.entities.annotations.GetEntity;
import jalse.entities.annotations.NewEntity;
import jalse.entities.annotations.SetAttribute;
import jalse.entities.annotations.StreamEntities;
import jalse.misc.ProxyCache;
import jalse.misc.ProxyCache.CacheHandler;
import jalse.misc.ProxyCache.ProxyFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A utility for validating {@link Entity} subclasses and creating proxies of these. The proxies are
 * cached for performance reasons but can be managed using this utility. It is possible to create a
 * new uncached proxy instance with {@link #uncachedProxyOfEntity(Entity, Class)}.<br>
 * <br>
 * An entity subclass can have the following method definitions: <br>
 * 1) Setting an attribute of type (will remove if argument passed is null).<br>
 * 2) Getting an attribute of type. <br>
 * 3) Getting an entity as type. <br>
 * 4) Creating a new entity of type. <br>
 * 5) Getting a Set of all of the child entities of or as type.<br>
 * 6) Getting a Stream of all of the child entities of or as type.<br>
 * <br>
 * For an entity proxy to be created the type be validated: <br>
 * 1. All attributes types must not be primitives (can be null).<br>
 * 2. Can only have super types that are (or are subclasses of) Entity. <br>
 * 3. Must only contain default or annotated methods:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;a) {@link SetAttribute} <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;b) {@link GetAttribute} <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;c) {@link GetEntity} <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;d) {@link NewEntity}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;e) {@link GetEntities}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;f) {@link StreamEntities}<br>
 * <br>
 * NOTE: The javadoc for the annotations provide more information about how to define the method.<br>
 * <br>
 * An example entity type:
 *
 * <pre>
 * <code>
 * public interface Ghost extends Entity {
 * 
 * 	{@code @GetAttribute("scary")}
 * 	Optional{@code <Boolean>} isScary();
 * 
 * 	{@code @SetAttribute("scary")}
 * 	void setScary(Boolean scary);
 * }
 * 
 * Entity e; // Previously created entity
 * 
 * Ghost evilGhost = Entities.asType(e, Ghost.class);
 * if (evilGhost.isScary()) {
 * 	// AAAAAH!
 * }
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 *
 * @see #proxyOfEntity(Entity, Class)
 * @see ProxyCache
 *
 */
public final class EntityProxies {

    private static class EntityTypeFactory implements ProxyFactory {

	@Override
	public CacheHandler newHandler(final Object obj, final Class<?> type) {
	    return new EntityTypeHandler(obj, type);
	}

	@Override
	public boolean validate(final Class<?> type) {
	    logger.info(String.format("Validating type: %s", type));
	    return validateTree(type, new HashSet<>(Collections.singleton(Entity.class)));
	}
    }

    private static class EntityTypeHandler extends CacheHandler {

	public EntityTypeHandler(final Object obj, final Class<?> type) {
	    super(obj, type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	    final Entity entity = (Entity) getOwner();
	    if (entity == null) {
		throw new IllegalStateException("Entity reference lost");
	    }

	    /*
	     * Not Entity subclass.
	     */
	    final Class<?> declaringClazz = method.getDeclaringClass();
	    if (Entity.class.equals(declaringClazz) || !Entity.class.isAssignableFrom(declaringClazz)) {
		return method.invoke(entity, args);
	    }

	    /*
	     * Default methods.
	     */
	    if (method.isDefault()) {
		return lookups.get(declaringClazz).unreflectSpecial(method, declaringClazz).bindTo(proxy)
			.invokeWithArguments(args);
	    }

	    /*
	     * Entity type method info.
	     */
	    final EntityTypeMethodInfo etmi = methodInfos.get(method);

	    /*
	     * Getting attributes.
	     */
	    if (etmi.forAnnotationType(GetAttribute.class)) {
		if (etmi.opt) {
		    return entity.getOptAttribute(etmi.attrType);
		} else {
		    return entity.getAttribute(etmi.attrType);
		}
	    }

	    /*
	     * Adding / removing attributes.
	     */
	    if (etmi.forAnnotationType(SetAttribute.class)) {
		if (etmi.opt) {
		    if (args[0] != null) {
			return entity.setOptAttribute(etmi.attrType, args[0]);
		    } else {
			return entity.removeOptAttribute(etmi.attrType);
		    }
		} else {
		    if (args[0] != null) {
			return entity.setAttribute(etmi.attrType, args[0]);
		    } else {
			return entity.removeAttribute(etmi.attrType);
		    }
		}
	    }

	    /*
	     * New entities.
	     */
	    if (etmi.forAnnotationType(NewEntity.class)) {
		if (args != null && args.length == 1) {
		    if (args[0] instanceof UUID) {
			return entity.newEntity((UUID) args[0], (Class<? extends Entity>) etmi.entityType);

		    } else {
			return entity
				.newEntity((Class<? extends Entity>) etmi.entityType, (AttributeContainer) args[0]);
		    }
		} else if (args != null && args.length == 2) {
		    return entity.newEntity((UUID) args[0], (Class<? extends Entity>) etmi.entityType,
			    (AttributeContainer) args[1]);
		} else {
		    return entity.newEntity((Class<? extends Entity>) etmi.entityType);
		}
	    }

	    /*
	     * Get entity as type.
	     */
	    if (etmi.forAnnotationType(GetEntity.class)) {
		if (etmi.opt) {
		    return entity.getEntityAsType((UUID) args[0], (Class<? extends Entity>) etmi.entityType);
		} else {
		    return entity.getOptEntityAsType((UUID) args[0], (Class<? extends Entity>) etmi.entityType);
		}
	    }

	    /*
	     * Stream entities.
	     */
	    if (etmi.forAnnotationType(StreamEntities.class)) {
		if (((StreamEntities) etmi.annotation).ofType()) {
		    return entity.streamEntitiesOfType((Class<? extends Entity>) etmi.entityType);
		} else {
		    return entity.streamEntitiesAsType((Class<? extends Entity>) etmi.entityType);
		}
	    }

	    /*
	     * Get entities.
	     */
	    if (etmi.forAnnotationType(GetEntities.class)) {
		if (((GetEntities) etmi.annotation).ofType()) {
		    return entity.getEntitiesOfType((Class<? extends Entity>) etmi.entityType);
		} else {
		    return entity.getEntitiesAsType((Class<? extends Entity>) etmi.entityType);
		}
	    }

	    throw new UnsupportedOperationException();
	}
    }

    private static class EntityTypeMethodInfo {

	private final Annotation annotation;
	private boolean opt;
	private Class<?> entityType;
	private NamedAttributeType<Object> attrType;

	private EntityTypeMethodInfo(final Annotation annotation) {
	    this.annotation = annotation;
	    entityType = null;
	    opt = false;
	    attrType = null;
	}

	public boolean forAnnotationType(final Class<? extends Annotation> type) {
	    return annotation.annotationType().equals(type);
	}
    }

    private static final Logger logger = Logger.getLogger(EntityProxies.class.getName());

    private static List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(GetAttribute.class,
	    SetAttribute.class, StreamEntities.class, GetEntities.class, GetEntity.class, NewEntity.class);

    private static final ProxyCache typeCache = new ProxyCache(new EntityTypeFactory());

    private static final ConcurrentMap<Method, EntityTypeMethodInfo> methodInfos = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Lookup> lookups = new ConcurrentHashMap<>();

    private static final Constructor<Lookup> LOOKUP_CONSTRUCTOR;

    static {
	try {
	    LOOKUP_CONSTRUCTOR = Lookup.class.getDeclaredConstructor(Class.class, int.class);
	    LOOKUP_CONSTRUCTOR.setAccessible(true);
	} catch (final Exception e) {
	    throw new Error("Could not get private Lookup constructor instance", e);
	}
    }

    private static Type firstGenericTypeArg(final Type pt) {
	return ((ParameterizedType) pt).getActualTypeArguments()[0];
    }

    private static boolean isPrimitive(final Type t) {
	return t instanceof Class<?> && ((Class<?>) t).isPrimitive();
    }

    /**
     * Checks to see if the supplied entity is a proxy.
     *
     * @param e
     *            Possible proxy to check.
     * @return Whether the entity is a proxy.
     */
    public static boolean isProxyEntity(final Entity e) {
	return Proxy.isProxyClass(e.getClass()) && Proxy.getInvocationHandler(e) instanceof EntityTypeHandler;
    }

    private static Lookup newLookupInstance(final Class<?> type) {
	try {
	    return LOOKUP_CONSTRUCTOR.newInstance(type, Lookup.PRIVATE);
	} catch (final Exception e) {
	    throw new RuntimeException(String.format("Could not create Lookup instance for %s", type), e);
	}
    }

    /**
     * Gets or creates a proxy of the supplied entity as the suppied type.
     *
     * @param e
     *            Entity to wrap.
     * @param type
     *            Entity type.
     * @return Entity proxy of type.
     *
     * @see #validateEntityType(Class)
     */
    public static <T extends Entity> T proxyOfEntity(final Entity e, final Class<T> type) {
	validateEntityType(type);
	return typeCache.getOrNew(e, type);
    }

    /**
     * Removes all proxies in the cache.
     */
    public static void removeAllProxies() {
	typeCache.removeAll();
	lookups.clear();
	methodInfos.clear();
    }

    /**
     * Removes all proxies of the supplied type.
     *
     * @param type
     *            Entity type.
     */
    public static void removeAllProxiesOfType(final Class<? extends Entity> type) {
	typeCache.invalidateType(type);
	lookups.remove(type);
	for (final Method m : type.getDeclaredMethods()) {
	    methodInfos.compute(m, (k, v) -> null);
	}
    }

    /**
     * Removes all proxies of a supplied entity.
     *
     * @param e
     *            Entity to remove proxies for.
     */
    public static void removeProxiesOfEntity(final Entity e) {
	typeCache.removeAll(e);
    }

    /**
     * Removes a specific proxy of an entity.
     *
     * @param e
     *            Entity remove for.
     * @param type
     *            Entity type.
     */
    public static void removeProxyOfEntity(final Entity e, final Class<? extends Entity> type) {
	typeCache.remove(e, type);
    }

    private static Class<?> toClass(final Type type) {
	if (type instanceof Class) {
	    return (Class<?>) type;
	} else if (type instanceof ParameterizedType) { // Resolve
	    return toClass(((ParameterizedType) type).getRawType());
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Creates a new uncached proxy of an entity as the supplied type.
     *
     * @param e
     *            Entity to wrap.
     * @param type
     *            Entity type.
     * @return New uncached proxy instance of entity.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T uncachedProxyOfEntity(final Entity e, final Class<T> type) {
	logger.info(String.format("Validating type: %s", type));
	if (!validateTree(type, new HashSet<>(Collections.singleton(Entity.class)))) {
	    throwRE(INVALID_ENTITY_TYPE);
	}
	return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },
		new EntityTypeHandler(e, type));
    }

    /**
     * Validates a specified Entity type according the criteria defined above. The ancestor
     * {@code interface} {@link Entities} is considered to be invalid.
     *
     * @param type
     *            Entity type to validate.
     * @throws IllegalArgumentException
     *             If the Entity type fails validation.
     */
    public static void validateEntityType(final Class<? extends Entity> type) {
	if (Entity.class.equals(type) || !typeCache.validateType(type)) {
	    throwRE(INVALID_ENTITY_TYPE);
	}
    }

    private static boolean validateTree(final Class<?> type, final Set<Class<?>> validated) {
	if (!Entity.class.isAssignableFrom(type)) { // Not Entity or subclass.
	    return false;
	}

	final Map<Method, EntityTypeMethodInfo> resolvedMethods = new HashMap<>();
	final Set<Class<?>> referencedtypes = new HashSet<>();

	for (final Method method : type.getDeclaredMethods()) {
	    /*
	     * Handler annotation.
	     */
	    Annotation annotation = null;
	    for (final Annotation a : method.getAnnotations()) {
		if (ANNOTATIONS.contains(a.annotationType())) {
		    if (annotation != null) {
			return false;
		    }
		    annotation = a;
		}
	    }

	    final boolean isDefaultOrStatic = method.isDefault() || Modifier.isStatic(method.getModifiers());

	    /*
	     * Skip un-annotated default methods.
	     */
	    if (annotation == null && isDefaultOrStatic) {
		continue;
	    } else if (annotation == null || isDefaultOrStatic) {
		return false;
	    }

	    /*
	     * Method info.
	     */
	    final Type[] params = method.getGenericParameterTypes();
	    final boolean hasParams = params != null && params.length > 0;
	    final Type returnType = method.getGenericReturnType();
	    final boolean hasReturnType = !Void.TYPE.equals(returnType);

	    /*
	     * getAttribute(Class) / getOptAttribute(Class)
	     */
	    if (GetAttribute.class.equals(annotation.annotationType())) {
		final String name = ((GetAttribute) annotation).value();
		if (!hasReturnType || hasParams || name.length() == 0) {
		    return false;
		}

		Type attrType = returnType;
		boolean opt = false;

		if (Optional.class.equals(toClass(returnType))) {
		    attrType = firstGenericTypeArg(returnType);
		    opt = true;
		} else if (isPrimitive(returnType)) {
		    return false; // Should not be primitive.
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.attrType = Attributes.newNamedUnknownType(name, attrType);
		etmi.opt = opt;

		resolvedMethods.put(method, etmi);
		continue;
	    }

	    /*
	     * addAttribute(Attribute) / addOptAttribute(Attribute) / removeAttribute(Class) /
	     * removeOptAttribute(Class)
	     */
	    if (SetAttribute.class.equals(annotation.annotationType())) {
		final String name = ((SetAttribute) annotation).value();
		if (!hasParams || params.length != 1 || name.length() == 0) {
		    return false;
		}

		if (isPrimitive(params[0])) {
		    return false; // Should not be primitive.
		}

		final Type attrType = params[0];
		boolean opt = false;

		if (hasReturnType) {
		    Type returnAttrType = returnType;

		    if (Optional.class.equals(toClass(returnType))) {
			returnAttrType = firstGenericTypeArg(returnType);
			opt = true;
		    }

		    if (!attrType.equals(returnAttrType)) {
			return false; // Should match.
		    }
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.attrType = Attributes.newNamedUnknownType(name, attrType);
		etmi.opt = opt;

		resolvedMethods.put(method, etmi);
		continue;
	    }

	    /*
	     * newEntity(Class) / newEntity(Class, AttributeContainer) / newEntity(UUID, class) /
	     * newEntity(UUID, Class, AttributeContainer)
	     */
	    if (NewEntity.class.equals(annotation.annotationType())) {
		if (!hasReturnType || !(!hasParams || hasParams && params.length <= 2)) {
		    return false;
		}

		if (hasParams) { // UUID or AttributeContainer / UUID and AttributeContainer
		    final Class<?> paramOne = toClass(params[0]);
		    if (!UUID.class.equals(paramOne) && !AttributeContainer.class.equals(paramOne)) {
			return false;
		    } else if (params.length == 2) {
			final Class<?> paramTwo = toClass(params[1]);
			if (!(UUID.class.equals(paramOne) && AttributeContainer.class.equals(paramTwo))) {
			    return false;
			}
		    }
		}

		final Class<?> entityType = toClass(returnType);
		if (!entityType.isInterface() || Entity.class.equals(entityType)
			|| !Entity.class.isAssignableFrom(entityType)) {
		    return false;
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.entityType = entityType;

		resolvedMethods.put(method, etmi);
		referencedtypes.add(entityType);
		continue;
	    }

	    /*
	     * getEntityAsType(Class)
	     */
	    if (GetEntity.class.equals(annotation.annotationType())) {
		if (!hasReturnType || !hasParams || params.length != 1 || !UUID.class.equals(toClass(params[0]))) {
		    return false;
		}

		Class<?> entityType = toClass(returnType);
		boolean opt = false;

		if (Optional.class.equals(entityType)) {
		    entityType = toClass(firstGenericTypeArg(returnType));
		    opt = true;
		}

		if (!entityType.isInterface() || Entity.class.equals(entityType)
			|| !Entity.class.isAssignableFrom(entityType)) {
		    return false;
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.entityType = entityType;
		etmi.opt = opt;

		resolvedMethods.put(method, etmi);
		referencedtypes.add(entityType);
		continue;
	    }

	    /*
	     * streamEntitiesOfType(Class) / streamEntitiesAsType(Class)
	     */
	    if (StreamEntities.class.equals(annotation.annotationType())) {
		if (!hasReturnType || hasParams || !Stream.class.equals(toClass(returnType))) {
		    return false;
		}

		final Class<?> entityType = toClass(firstGenericTypeArg(returnType));

		if (!entityType.isInterface() || Entity.class.equals(entityType)
			|| !Entity.class.isAssignableFrom(entityType)) {
		    return false;
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.entityType = entityType;

		resolvedMethods.put(method, etmi);
		referencedtypes.add(entityType);
		continue;
	    }

	    /*
	     * getEntitiesOfType(Class) / getEntitiesAsType(Class)
	     */
	    if (GetEntities.class.equals(annotation.annotationType())) {
		if (!hasReturnType || hasParams || !Set.class.equals(toClass(returnType))) {
		    return false;
		}

		final Class<?> entityType = toClass(firstGenericTypeArg(returnType));

		if (!entityType.isInterface() || Entity.class.equals(entityType)
			|| !Entity.class.isAssignableFrom(entityType)) {
		    return false;
		}

		final EntityTypeMethodInfo etmi = new EntityTypeMethodInfo(annotation);
		etmi.entityType = entityType;

		resolvedMethods.put(method, etmi);
		referencedtypes.add(entityType);
		continue;
	    }

	    /*
	     * If not found above then it is not a valid Entity type method.
	     */
	    return false;
	}

	validated.add(type); // Avoid cyclic dependencies..

	referencedtypes.addAll(Arrays.asList(type.getInterfaces())); // Direct super types.

	/*
	 * Validate referenced types.
	 */
	for (final Class<?> ref : referencedtypes) {
	    if (validated.contains(ref)) {
		continue;
	    }

	    if (!validateTree(ref, validated)) {
		return false;
	    }
	}

	lookups.computeIfAbsent(type, EntityProxies::newLookupInstance);
	methodInfos.putAll(resolvedMethods);

	return true;
    }

    private EntityProxies() {
	throw new UnsupportedOperationException();
    }
}
