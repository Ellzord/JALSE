package jalse.entities;

import static jalse.misc.JALSEExceptions.INVALID_ENTITY_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import static jalse.misc.TypeParameterResolver.getTypeParameter;
import static jalse.misc.TypeParameterResolver.toClass;
import jalse.attributes.Attribute;
import jalse.listeners.EntityListener;
import jalse.misc.JALSEExceptions;
import jalse.misc.TypeParameterResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A utility for {@link Entity} related functionality (specifically wrapping
 * entities as other entity types). An Entity type allows entities to be used
 * like beans (get/set) and can be used to process entities of similar state or
 * function.<br>
 * <br>
 * An entity type has 4 types of method definitions: <br>
 * 1) Adding and removing Attribute of type (to remove call method with null
 * Attribute).<br>
 * 2) Getting an Attribute of type. <br>
 * 3) Getting a Set of all of the child entities marked as type.<br>
 * 4) Getting a Stream of all of the child entities markd as type.<br>
 * <br>
 * For an Entity type to be used it must be validated against the below
 * criteria: <br>
 * 1. Must be a subclass of Entity (can be indirect). <br>
 * 2. Can only have super types that are also subclasses of Entity. <br>
 * 3. The add/remove Attribute method: <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;a) Must return either {@code void} or
 * {@code Optional<Attribute_Type>}.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;b) Must have only one parameter
 * {@code Attribute_Type} (matching the return type if not {@code void}). <br>
 * 4. The get Attribute method: <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;a) Must return {@code Optional<Attribute_Type>}.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;b) Must have no parameters.<br>
 * 5. The Set Entity method: <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;a) Must return {@code Set<Entity_Type>}. <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;b) Must have no parameters.<br>
 * 6. The Stream Entity method: <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;a) Must return {@code Stream<Entity_Type}. <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;b) Must have no parameters. <br>
 * <br>
 * An example entity type:
 *
 * <pre>
 * <code>
 * public interface Car extends Entity {
 * 
 * 	Optional{@code<Load>} getLoad();
 * 
 * 	void setLoad(Load load);
 * }
 * 
 * Entity e; // Previously created entity
 * 
 * Car car = Entities.asType(e, Car.class);
 * Load load = car.getLoad();
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 *
 * @see Entity#markAsType(Class)
 * @see Entity#asType(Class)
 *
 */
public final class Entities {

    private static class EntityHandler implements InvocationHandler {

	private final Entity entity;

	public EntityHandler(final Entity Entity) {

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

	    throw new UnsupportedOperationException();
	}
    }

    private static class UnmodifiableDelegateEntityContainer implements EntityContainer {

	private final EntityContainer delegate;

	private UnmodifiableDelegateEntityContainer(final EntityContainer delegate) {

	    this.delegate = delegate;
	}

	@Override
	public boolean addEntityListener(final EntityListener listener) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entity> getEntities() {

	    return delegate != null ? delegate.getEntities() : Collections.emptySet();
	}

	@Override
	public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {

	    return delegate != null ? delegate.getEntitiesOfType(type) : Collections.emptySet();
	}

	@Override
	public Optional<Entity> getEntity(final UUID id) {

	    return delegate != null ? delegate.getEntity(id) : Optional.empty();
	}

	@Override
	public int getEntityCount() {

	    return delegate != null ? delegate.getEntityCount() : 0;
	}

	@Override
	public Set<UUID> getEntityIDs() {

	    return delegate != null ? delegate.getEntityIDs() : Collections.emptySet();
	}

	@Override
	public Set<? extends EntityListener> getEntityListeners() {

	    return delegate != null ? delegate.getEntityListeners() : Collections.emptySet();
	}

	@Override
	public void killEntities() {

	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean killEntity(final UUID id) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Entity newEntity() {

	    throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Entity> T newEntity(final Class<T> type) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Entity newEntity(final UUID id) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeEntityListener(final EntityListener listener) {

	    throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> streamEntities() {

	    return delegate != null ? delegate.streamEntities() : Stream.empty();
	}

	@Override
	public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {

	    return delegate != null ? delegate.streamEntitiesOfType(type) : Stream.empty();
	}
    }

    private static final Logger logger = Logger.getLogger(Entities.class.getName());

    private static final TypeParameterResolver OPTIONAL_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Optional.class, "T"));

    private static final TypeParameterResolver SET_RESOLVER = new TypeParameterResolver(
	    getTypeParameter(Set.class, "E"));

    private static final TypeParameterResolver STREAM_RESOLVER = new TypeParameterResolver(getTypeParameter(
	    Stream.class, "T"));

    private static Set<Class<?>> VALID_ENTITY_TYPES = new CopyOnWriteArraySet<Class<?>>() {

	private static final long serialVersionUID = -3273614078225830902L;

	{
	    add(Entity.class);
	}
    };

    @SuppressWarnings("unchecked")
    private static void addAncestors(final Set<Class<? extends Entity>> ancestry, final Class<?> type) {

	for (final Class<?> t : type.getInterfaces()) {

	    if (!t.equals(Entity.class) && ancestry.add((Class<? extends Entity>) t)) {

		addAncestors(ancestry, t);
	    }
	}
    }

    /**
     * Wraps an Entity as the supplied Entity type.
     *
     * @param Entity
     *            Entity to wrap.
     * @param type
     *            Entity type to wrap to.
     * @return The wrapped Entity.
     *
     * @throws NullPointerException
     *             If the Entity or Entity type are null.
     * @throws IllegalArgumentException
     *             If the Entity type does not meet the criteria defined above.
     *
     * @see Entities
     * @see JALSEExceptions#INVALID_ENTITY_TYPE
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T asType(final Entity Entity, final Class<T> type) {

	validateType(type);

	InvocationHandler handler = null;

	if (Proxy.isProxyClass(Entity.getClass())) {

	    handler = Proxy.getInvocationHandler(Entity);
	}

	return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type },
		handler instanceof EntityHandler ? handler : new EntityHandler(Entity));
    }

    /**
     * Creates an immutable empty entity container.
     *
     * @return Empty entity container.
     */
    public static EntityContainer emptyEntityContainer() {

	return new UnmodifiableDelegateEntityContainer(null);
    }

    /**
     * Gets all ancestors for the specified descendant type (not including
     * {@link Entity}).
     *
     * @param type
     *            Descendant type.
     * @return All ancestors or an empty set if its only ancestor is
     *         {@link Entity}.
     *
     * @throws IllegalArgumentException
     *             If the Entity type is invalid
     *
     * @see JALSEExceptions#INVALID_ENTITY_TYPE
     */
    public static Set<Class<? extends Entity>> getAncestry(final Class<? extends Entity> type) {

	validateType(type);

	final Set<Class<? extends Entity>> ancestry = new HashSet<>();

	addAncestors(ancestry, type);

	return ancestry;
    }

    /**
     * Checks if the specified type is equal to or a descendant from the
     * specified ancestor type.
     *
     * @param descendant
     *            Descendant type.
     * @param ancestor
     *            Ancestor type.
     * @return Whether the descendant is equal or descended from the ancestor
     *         type.
     */
    public static boolean isOrDescendant(final Class<? extends Entity> descendant,
	    final Class<? extends Entity> ancestor) {

	return ancestor.isAssignableFrom(descendant);
    }

    /**
     * Creates an immutable read-only delegate entity container for the supplied
     * container.
     *
     * @param container
     *            Container to delegate for.
     * @return Immutable entity container.
     */
    public static EntityContainer unmodifiableEntityContainer(final EntityContainer container) {

	return new UnmodifiableDelegateEntityContainer(Objects.requireNonNull(container));
    }

    /**
     * Validates a specified Entity type according the criteria defined above.
     * The ancestor {@code interface} {@link Entities} is considered to be
     * invalid.
     *
     * @param type
     *            Entity type to validate.
     * @throws IllegalArgumentException
     *             If the Entity type fails validation.
     */
    public static void validateType(final Class<? extends Entity> type) {

	if (type.equals(Entity.class)) {

	    throwRE(INVALID_ENTITY_TYPE);
	}

	validateType0(type);
    }

    private static void validateType0(final Class<?> clazz) {

	if (!Entity.class.isAssignableFrom(clazz)) {

	    throwRE(INVALID_ENTITY_TYPE);
	}

	if (!VALID_ENTITY_TYPES.contains(clazz)) {

	    /*
	     * Previously validated types.
	     */
	    final Set<Type> addRemoves = new HashSet<>();
	    final Set<Type> streams = new HashSet<>();
	    final Set<Type> sets = new HashSet<>();
	    final Set<Type> gets = new HashSet<>();

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
		 * getEntitiesOfType(Class) / streamEntitiesOfType(Class)
		 */
		if (hasParams && Entity.class.isAssignableFrom(toClass(params[0]))) {

		    /*
		     * Must be a subclass of Entity.
		     */
		    if (Entity.class.equals(params[0])) {

			throwRE(INVALID_ENTITY_TYPE);
		    }

		    final Class<?> setOrStreamClazz = toClass(returnType);
		    boolean isSet = false;
		    Type entityClazz = null;

		    /*
		     * Must be Set or Stream.
		     */
		    if (isSet = Set.class.equals(setOrStreamClazz)) {

			entityClazz = SET_RESOLVER.resolve(returnType);
		    }
		    else if (Stream.class.equals(setOrStreamClazz)) {

			entityClazz = STREAM_RESOLVER.resolve(returnType);
		    }
		    else {

			throwRE(INVALID_ENTITY_TYPE);
		    }

		    /*
		     * Must match parameter.
		     */
		    if (!params[0].equals(entityClazz)) {

			throwRE(INVALID_ENTITY_TYPE);
		    }

		    if (!(isSet ? sets : streams).add(params[0])) {

			logger.warning(String.format(
				isSet ? "Entity type (%s) has multiple Set definitions for Entity (%s)"
					: "Entity type (%s) has multiple Stream definitions for Entity (%s)", clazz
					.getName(), params[0].getTypeName()));
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
		 * If not found above then it is not a valid Entity type method.
		 */
		throwRE(INVALID_ENTITY_TYPE);
	    }

	    logger.info(String.format("Entity type (%s) is valid", clazz.getName()));

	    VALID_ENTITY_TYPES.add(clazz);

	    /*
	     * Validate all super types.
	     */
	    Stream.of(clazz.getInterfaces()).forEach(Entities::validateType0);
	}
    }

    private Entities() {

	throw new UnsupportedOperationException();
    }
}
