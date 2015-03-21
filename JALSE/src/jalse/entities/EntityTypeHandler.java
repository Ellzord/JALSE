package jalse.entities;

import static jalse.misc.JALSEExceptions.INVALID_ENTITY_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import static jalse.misc.TypeParameterResolver.getTypeParameter;
import static jalse.misc.TypeParameterResolver.toClass;
import jalse.attributes.Attribute;
import jalse.misc.TypeParameterResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import java.util.stream.Stream;

class EntityTypeHandler implements InvocationHandler {

    private static final Logger logger = Logger.getLogger(EntityTypeHandler.class.getName());

    private static final TypeParameterResolver OPTIONAL_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Optional.class, "T"));

    private static final TypeParameterResolver SET_RESOLVER = new TypeParameterResolver(
	    getTypeParameter(Set.class, "E"));

    private static final TypeParameterResolver STREAM_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Stream.class, "T"));

    @SuppressWarnings("serial")
    private static Set<Class<?>> VALID_ENTITY_TYPES = new CopyOnWriteArraySet<Class<?>>() {

	{
	    add(Entity.class);
	}
    };

    public static void validateType(final Class<?> clazz) {
	if (!Entity.class.isAssignableFrom(clazz)) {
	    throwRE(INVALID_ENTITY_TYPE);
	}

	if (!VALID_ENTITY_TYPES.contains(clazz)) {
	    /*
	     * Previously validated types.
	     */
	    final Set<Type> addRemoves = new HashSet<>();
	    final Set<Type> gets = new HashSet<>();
	    final Set<Class<?>> streams = new HashSet<>();
	    final Set<Class<?>> sets = new HashSet<>();

	    for (final Method method : clazz.getDeclaredMethods()) {
		/*
		 * Method info.
		 */
		final Type[] params = method.getParameterTypes();
		final boolean hasParams = params != null && params.length > 0;
		final Type returnType = method.getGenericReturnType();
		final boolean hasReturnType = !Void.TYPE.equals(returnType);

		/*
		 * All methods that accept parameters have only one.
		 */
		if (hasParams && params.length != 1) {
		    throwRE(INVALID_ENTITY_TYPE);
		}

		/*
		 * addAttributeOfType(Attribute) / removeAttributeOfType(Class)
		 */
		if (hasParams && Attribute.class.isAssignableFrom(toClass(params[0]))) {
		    /*
		     * Must be a subclass of Attribute.
		     */
		    if (Attribute.class.equals(params[0])) {
			throwRE(INVALID_ENTITY_TYPE);
		    }

		    /*
		     * Must be void or Optional<Attribute>.
		     */
		    if (hasReturnType) {
			if (!Optional.class.equals(toClass(returnType))) {
			    throwRE(INVALID_ENTITY_TYPE);
			}

			final Type attributeClazz = OPTIONAL_RESOLVER.resolve(returnType);

			/*
			 * Must match parameter.
			 */
			if (!params[0].equals(attributeClazz)) {
			    throwRE(INVALID_ENTITY_TYPE);
			}
		    }

		    if (!addRemoves.add(params[0])) {
			logger.warning(String.format(
				"Entity type (%s) has multiple add/remove definitions for Atribute (%s)",
				clazz.getName(), params[0].getTypeName()));
		    }

		    continue;
		}

		/*
		 * getAttributeOfType(Class)
		 */
		if (!hasParams && hasReturnType && Optional.class.equals(toClass(returnType))) {
		    final Class<?> attributeClazz = toClass(OPTIONAL_RESOLVER.resolve(returnType));

		    /*
		     * Must be subclass of Attribute.
		     */
		    if (Attribute.class.equals(attributeClazz) || !Attribute.class.isAssignableFrom(attributeClazz)) {
			throwRE(INVALID_ENTITY_TYPE);
		    }

		    if (!gets.add(attributeClazz)) {
			logger.warning(String.format("Entity type (%s) has multiple get definitions for Atribute (%s)",
				clazz.getTypeName(), attributeClazz.getTypeName()));
		    }

		    continue;
		}

		/*
		 * getOrNullAttributeOfType(Class)
		 */
		if (!hasParams && hasReturnType && Attribute.class.isAssignableFrom(toClass(returnType))) {
		    /*
		     * Must be subclass of Attribute.
		     */
		    if (Attribute.class.equals(returnType)) {
			throwRE(INVALID_ENTITY_TYPE);
		    }

		    if (!gets.add(returnType)) {
			logger.warning(String.format("Entity type (%s) has multiple get definitions for Atribute (%s)",
				clazz.getTypeName(), returnType.getTypeName()));
		    }

		    continue;
		}

		/*
		 * streamEntitiesOfType(Class)
		 */
		if (!hasParams && hasReturnType && Stream.class.equals(toClass(returnType))) {
		    final Class<?> entityClazz = toClass(STREAM_RESOLVER.resolve(returnType));

		    /*
		     * Must be a subclass of Entity.
		     */
		    if (Entity.class.equals(entityClazz) || !Entity.class.isAssignableFrom(entityClazz)) {
			throwRE(INVALID_ENTITY_TYPE);
		    }

		    if (!streams.add(entityClazz)) {
			logger.warning(String.format(
				"Entity type (%s) has multiple Stream definitions for Entity (%s)", clazz.getName(),
				params[0].getTypeName()));
		    }

		    continue;
		}

		/*
		 * getEntitiesOfType(Class)
		 */
		if (!hasParams && hasReturnType && Set.class.equals(toClass(returnType))) {
		    final Class<?> entityClazz = toClass(SET_RESOLVER.resolve(returnType));

		    /*
		     * Must be a subclass of Entity.
		     */
		    if (Entity.class.equals(entityClazz) || !Entity.class.isAssignableFrom(entityClazz)) {
			throwRE(INVALID_ENTITY_TYPE);
		    }

		    if (!sets.add(entityClazz)) {
			logger.warning(String.format("Entity type (%s) has multiple Set definitions for Entity (%s)",
				clazz.getName(), params[0].getTypeName()));
		    }

		    continue;
		}

		/*
		 * If not found above then it is not a valid Entity type method.
		 */
		throwRE(INVALID_ENTITY_TYPE);
	    }

	    logger.info(String.format("Entity type (%s) is valid", clazz.getName()));

	    VALID_ENTITY_TYPES.add(clazz);

	    /*
	     * Validate referenced types.
	     */
	    final Set<Class<?>> referenced = new HashSet<>();
	    referenced.addAll(streams);
	    referenced.addAll(sets);
	    referenced.forEach(EntityTypeHandler::validateType);

	    /*
	     * Validate all super types.
	     */
	    Stream.of(clazz.getInterfaces()).forEach(EntityTypeHandler::validateType);
	}
    }

    private final Entity entity;

    EntityTypeHandler(final Entity Entity) {
	entity = Entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	final Class<?> declaringClazz = method.getDeclaringClass();

	/*
	 * Not entity type methods!
	 */
	if (Entity.class.equals(declaringClazz) || !Entity.class.isAssignableFrom(declaringClazz)) {
	    return method.invoke(entity, args);
	}

	/*
	 * Method info.
	 */
	final Type[] params = method.getParameterTypes();
	final boolean hasParams = params != null && params.length > 0;
	final Type returnType = method.getGenericReturnType();
	final boolean hasReturnType = !Void.TYPE.equals(returnType);

	/*
	 * addAttributeOfType(Attribute) / removeAttributeOfType(Class)
	 */
	if (hasParams && Attribute.class.isAssignableFrom(toClass(params[0]))) {
	    return args[0] != null ? entity.addAttributeOfType((Attribute) args[0]) : entity
		    .removeAttributeOfType((Class<? extends Attribute>) params[0]);
	}

	/*
	 * getEntitiesOfType(Class) / streamEntitiesOfType(Class)
	 */
	if (hasParams && Entity.class.isAssignableFrom(toClass(params[0]))) {
	    final Class<?> clazz = toClass(returnType);

	    if (Set.class.equals(clazz)) {
		return entity.getEntitiesOfType((Class<? extends Entity>) SET_RESOLVER.resolve(returnType));
	    }

	    if (Stream.class.equals(clazz)) {
		return entity.streamEntitiesOfType((Class<? extends Entity>) STREAM_RESOLVER.resolve(returnType));
	    }

	    throw new UnsupportedOperationException();
	}

	/*
	 * getAttributeOfType(Class)
	 */
	if (hasReturnType && Optional.class.equals(toClass(returnType))) {
	    return entity.getAttributeOfType((Class<? extends Attribute>) OPTIONAL_RESOLVER.resolve(returnType));
	}

	/*
	 * getOrNullAttributeOfType(Class)
	 */
	if (hasReturnType && Attribute.class.isAssignableFrom(toClass(returnType))) {
	    return entity.getOrNullAttributeOfType((Class<? extends Attribute>) returnType);
	}

	throw new UnsupportedOperationException();
    }
}