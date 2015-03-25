package jalse.entities;

import static jalse.misc.TypeParameterResolver.getTypeParameter;
import static jalse.misc.TypeParameterResolver.toClass;
import jalse.attributes.Attribute;
import jalse.entities.annotations.GetAttribute;
import jalse.entities.annotations.GetEntities;
import jalse.entities.annotations.GetEntity;
import jalse.entities.annotations.NewEntity;
import jalse.entities.annotations.SetAttribute;
import jalse.entities.annotations.StreamEntities;
import jalse.misc.ProxyCache;
import jalse.misc.ProxyCache.CacheHandler;
import jalse.misc.ProxyCache.ProxyFactory;
import jalse.misc.TypeParameterResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

class EntityProxies {

    private static class EntityTypeFactory implements ProxyFactory {

	@Override
	public CacheHandler createHandler(final Object obj, final Class<?> type) {
	    return new EntityTypeHandler(obj, type);
	}

	@Override
	public boolean validate(final Class<?> type) {
	    return validateType(type, new HashSet<>());
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
	     * Default methods.
	     */
	    if (method.isDefault()) {
		return method.invoke(entity, args);
	    }

	    /*
	     * Resolved class from validation.
	     */
	    final EntityTypeMethod etm = getTypeMethod(method);

	    /*
	     * Not entity type methods!
	     */
	    if (etm == null) {
		return method.invoke(entity, args);
	    }

	    /*
	     * getAttributeOfType(Class) / getOrNullAttributeOfType(Class)
	     */
	    if (etm.forAnnotationType(GetAttribute.class)) {
		if (etm.isOrNull()) {
		    return entity.getOrNullAttributeOfType((Class<? extends Attribute>) etm.getResolved());
		} else {
		    return entity.getAttributeOfType((Class<? extends Attribute>) etm.getResolved());
		}
	    }

	    /*
	     * addAttributeOfType(Attribute) / addOrNullAttributeOfType(Attribute) /
	     * removeAttributeOfType(Class) / removeOrNullAttributeOfType(Class)
	     */
	    if (etm.forAnnotationType(SetAttribute.class)) {
		final Attribute attr = (Attribute) args[0];

		if (etm.isOrNull()) {
		    if (attr != null) {
			return entity.addOrNullAttributeOfType((Attribute) args[0]);
		    } else {
			return entity.removeOrNullAttributeOfType((Class<? extends Attribute>) etm.getResolved());
		    }
		} else {
		    if (attr != null) {
			return entity.addAttributeOfType((Attribute) args[0]);
		    } else {
			return entity.removeAttributeOfType((Class<? extends Attribute>) etm.getResolved());
		    }
		}
	    }

	    /*
	     * newEntity(Class) / newEntity(UUID, Class)
	     */
	    if (etm.forAnnotationType(NewEntity.class)) {
		if (args != null && args.length == 1) {
		    return entity.newEntity((UUID) args[0], (Class<? extends Entity>) etm.getResolved());

		} else {
		    return entity.newEntity((Class<? extends Entity>) etm.getResolved());
		}
	    }

	    /*
	     * getEntityAsType(Class)
	     */
	    if (etm.forAnnotationType(GetEntity.class)) {
		if (etm.isOrNull()) {
		    return entity.getOrNullEntityAsType((UUID) args[0], (Class<? extends Entity>) etm.getResolved());
		} else {
		    return entity.getEntityAsType((UUID) args[0], (Class<? extends Entity>) etm.getResolved());
		}
	    }

	    /*
	     * streamEntitiesOfType(Class) / streamEntitiesAsType(Class)
	     */
	    if (etm.forAnnotationType(StreamEntities.class)) {
		if (((StreamEntities) etm.getAnnotation()).ofType()) {
		    return entity.streamEntitiesOfType((Class<? extends Entity>) etm.getResolved());
		} else {
		    return entity.streamEntitiesAsType((Class<? extends Entity>) etm.getResolved());
		}
	    }

	    /*
	     * getEntitiesOfType(Class) / getEntitiesAsType(Class)
	     */
	    if (etm.forAnnotationType(GetEntities.class)) {
		if (((GetEntities) etm.getAnnotation()).ofType()) {
		    return entity.getEntitiesOfType((Class<? extends Entity>) etm.getResolved());
		} else {
		    return entity.getEntitiesAsType((Class<? extends Entity>) etm.getResolved());
		}
	    }

	    throw new UnsupportedOperationException();
	}
    }

    private static class EntityTypeMethod {

	private final Annotation annotation;
	private final Class<?> resolved;
	private final boolean orNull;

	public EntityTypeMethod(final Annotation annotation, final Class<?> resolved, final boolean orNull) {
	    this.annotation = annotation;
	    this.resolved = resolved;
	    this.orNull = orNull;
	}

	public boolean forAnnotationType(final Class<? extends Annotation> type) {
	    return annotation.annotationType().equals(type);
	}

	public Annotation getAnnotation() {
	    return annotation;
	}

	public Class<?> getResolved() {
	    return resolved;
	}

	public boolean isOrNull() {
	    return orNull;
	}
    }

    private static final TypeParameterResolver OPTIONAL_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Optional.class, "T"));

    private static final TypeParameterResolver SET_RESOLVER = new TypeParameterResolver(
	    getTypeParameter(Set.class, "E"));

    private static final TypeParameterResolver STREAM_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Stream.class, "T"));

    private static List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(GetAttribute.class,
	    SetAttribute.class, StreamEntities.class, GetEntities.class, GetEntity.class, NewEntity.class);

    private static final ProxyCache typeCache = new ProxyCache(new EntityTypeFactory());

    private static final Map<Class<?>, Map<Method, EntityTypeMethod>> typesToMethods = new ConcurrentHashMap<>();

    private static EntityTypeMethod getTypeMethod(final Method method) {
	final Map<Method, EntityTypeMethod> methods = typesToMethods.get(method.getDeclaringClass());
	return methods != null ? methods.get(method) : null;
    }

    public static <T extends Entity> T proxyOfEntity(final Entity e, final Class<T> type) {
	return typeCache.getOrCreate(e, type);
    }

    public static void removeProxiesOfEntity(final Entity e) {
	typeCache.removeAll(e);
    }

    public static void removeProxyOfEntity(final Entity e, final Class<? extends Entity> type) {
	typeCache.remove(e, type);
    }

    private static boolean validateType(final Class<?> type, final Set<Class<?>> validated) {
	if (!Entity.class.isAssignableFrom(type)) { // Not Entity or subclass.
	    return false;
	} else if (Entity.class.equals(type)) { // Can stop here.
	    return true;
	}

	final Map<Method, EntityTypeMethod> resolvedMethods = new HashMap<>();
	final Set<Class<?>> referencedtypes = new HashSet<>();

	for (final Method method : type.getDeclaredMethods()) {

	    /*
	     * Skip default methods.
	     */
	    if (method.isDefault()) {
		continue;
	    }

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
	    if (annotation == null) { // Needs annotation..
		return false;
	    }

	    /*
	     * Method info.
	     */
	    final Type[] params = method.getParameterTypes();
	    final boolean hasParams = params != null && params.length > 0;
	    final Type returnType = method.getGenericReturnType();
	    final boolean hasReturnType = !Void.TYPE.equals(returnType);

	    /*
	     * getAttributeOfType(Class) / getOrNullAttributeOfType(Class)
	     */
	    if (GetAttribute.class.equals(annotation.annotationType())) {
		if (!hasReturnType || hasParams) {
		    return false;
		}

		Class<?> resolved = toClass(returnType);
		boolean orNull = true;

		if (Optional.class.equals(resolved)) {
		    resolved = toClass(OPTIONAL_RESOLVER.resolve(returnType));
		    orNull = false;
		}

		if (Modifier.isAbstract(resolved.getModifiers()) || resolved.isInterface()
			|| !Attribute.class.isAssignableFrom(resolved)) {
		    return false;
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, orNull));
		continue;
	    }

	    /*
	     * addAttributeOfType(Attribute) / addOrNullAttributeOfType(Attribute) /
	     * removeAttributeOfType(Class) / removeOrNullAttributeOfType(Class)
	     */
	    if (SetAttribute.class.equals(annotation.annotationType())) {
		if (!hasParams || params.length != 1) {
		    return false;
		}

		final Class<?> resolved = toClass(params[0]);
		boolean orNull = true;

		if (Modifier.isAbstract(resolved.getModifiers()) || resolved.isInterface()
			|| !Attribute.class.isAssignableFrom(resolved)) {
		    return false;
		}

		if (hasReturnType) {
		    Class<?> returnTypeClazz = toClass(returnType);

		    if (Optional.class.equals(returnTypeClazz)) {
			returnTypeClazz = toClass(OPTIONAL_RESOLVER.resolve(returnType));
			orNull = false;
		    }

		    if (!resolved.equals(returnTypeClazz)) {
			return false;
		    }
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, orNull));
		continue;
	    }

	    /*
	     * newEntity(Class) / newEntity(UUID, Class)
	     */
	    if (NewEntity.class.equals(annotation.annotationType())) {
		if (!hasReturnType || !(!hasParams || hasParams && params.length == 1 && UUID.class.equals(params[0]))) {
		    return false;
		}
		final Class<?> resolved = toClass(returnType);
		if (!resolved.isInterface() || Entity.class.equals(resolved)
			|| !Entity.class.isAssignableFrom(resolved)) {
		    return false;
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, false));
		referencedtypes.add(resolved);
		continue;
	    }

	    /*
	     * getEntityAsType(Class)
	     */
	    if (GetEntity.class.equals(annotation.annotationType())) {
		if (!hasReturnType || !hasParams || params.length != 1 || !UUID.class.equals(params[0])) {
		    return false;
		}
		Class<?> resolved = toClass(returnType);
		boolean orNull = true;

		if (Optional.class.equals(resolved)) {
		    resolved = toClass(OPTIONAL_RESOLVER.resolve(returnType));
		    orNull = false;
		}

		if (!resolved.isInterface() || Entity.class.equals(resolved)
			|| !Entity.class.isAssignableFrom(resolved)) {
		    return false;
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, orNull));
		referencedtypes.add(resolved);
		continue;
	    }

	    /*
	     * streamEntitiesOfType(Class) / streamEntitiesAsType(Class)
	     */
	    if (StreamEntities.class.equals(annotation.annotationType())) {
		if (!hasReturnType || hasParams) {
		    return false;
		}

		final Class<?> returnTypeClazz = toClass(returnType);

		if (!Stream.class.equals(returnTypeClazz)) {
		    return false;
		}

		final Class<?> resolved = toClass(STREAM_RESOLVER.resolve(returnType));

		if (!resolved.isInterface() || Entity.class.equals(resolved)
			|| !Entity.class.isAssignableFrom(resolved)) {
		    return false;
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, false));
		referencedtypes.add(resolved);
		continue;
	    }

	    /*
	     * getEntitiesOfType(Class) / getEntitiesAsType(Class)
	     */
	    if (GetEntities.class.equals(annotation.annotationType())) {
		if (!hasReturnType || hasParams) {
		    return false;
		}

		final Class<?> returnTypeClazz = toClass(returnType);

		if (!Set.class.equals(returnTypeClazz)) {
		    return false;
		}

		final Class<?> resolved = toClass(SET_RESOLVER.resolve(returnType));

		if (!resolved.isInterface() || Entity.class.equals(resolved)
			|| !Entity.class.isAssignableFrom(resolved)) {
		    return false;
		}

		resolvedMethods.put(method, new EntityTypeMethod(annotation, resolved, false));
		referencedtypes.add(resolved);
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

	    if (!validateType(ref, validated)) {
		return false;
	    }
	}

	typesToMethods.put(type, resolvedMethods);

	return true;
    }

    public static boolean validEntityType(final Class<?> type) {
	return !Entity.class.equals(type) && typeCache.isValidType(type);
    }

    private EntityProxies() {
	throw new UnsupportedOperationException();
    }
}
