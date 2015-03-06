package jalse.entities;

import static jalse.misc.JALSEExceptions.INVALID_ENTITY_TYPE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.entities.EntityVisitor.EntityVisitResult;
import jalse.misc.JALSEExceptions;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
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
 * 4) Getting a Stream of all of the child entities marked as type.<br>
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
 * &nbsp;&nbsp;&nbsp;&nbsp;a) Must return {@code Stream<Entity_Type>}. <br>
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
		handler instanceof EntityTypeHandler ? handler : new EntityTypeHandler(Entity));
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

	EntityTypeHandler.validateType(type);
    }

    /**
     * Walks through all entities (recursive). Walking can be stopped or
     * filtered based on the visit result returned.
     *
     * @param container
     *            Entity container.
     * @param visitor
     *            Entity visitor.
     *
     * @see EntityVisitor
     */
    public static void walkEntities(final EntityContainer container, final EntityVisitor visitor) {

	final EntityWalker walker = new EntityWalker(container, visitor);

	do {

	    walker.walk();
	} while (walker.isWalking());
    }

    /**
     * Gets the total entity count (recursive).
     *
     * @param container
     *            Entity container.
     *
     * @return Direct child entity count.
     */
    public int getEntityCountRecursively(final EntityContainer container) {

	final AtomicInteger result = new AtomicInteger();

	walkEntities(container, e -> {

	    result.incrementAndGet();
	    return EntityVisitResult.CONTINUE;
	});

	return result.get();
    }

    /**
     * Gets the IDs of all the entities (recursive).
     *
     * @param container
     *            Entity container.
     *
     * @return Set of all entity identifiers.
     */
    public Set<UUID> getEntityIDsRecursively(final EntityContainer container) {

	final Set<UUID> result = new HashSet<>();

	walkEntities(container, e -> {

	    result.add(e.getID());
	    return EntityVisitResult.CONTINUE;
	});

	return result;
    }

    /**
     * Provides a stream of entities from the container (recursive).
     *
     * @param container
     *            Entity container.
     *
     * @return A stream of entities in the container.
     */
    public Stream<Entity> streamEntitiesRecursively(final EntityContainer container) {

	return container.streamEntities().flatMap(c -> streamEntitiesRecursively(c));
    }

    /**
     * Gets a stream of all entities marked with the specified type (recursive).
     *
     * @param container
     *            Entity container.
     * @param type
     *            Entity type.
     * @return Stream of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     */
    public <T extends Entity> Stream<T> streamEntitiesOfTypeRecursively(final EntityContainer container,
	    final Class<T> type) {

	return container.streamEntitiesOfType(type).flatMap(c -> streamEntitiesOfTypeRecursively(c, type));
    }

    /**
     * Gets any entity within the container.
     *
     * @param container
     *            Entity container.
     *
     * @return Gets an Optional of the resulting entity or an empty Optional if
     *         it was not found.
     */
    public static Optional<Entity> anyEntity(final EntityContainer container) {

	return container.streamEntities().findAny();
    }

    /**
     * Gets any entity within the container marked with the specified type.
     *
     * @param container
     *            Entity container.
     *
     * @param type
     *            Entity type.
     *
     * @return Gets an Optional of the resulting entity or an empty Optional if
     *         it was not found.
     *
     * @see Entity#markAsType(Class)
     */
    public static <T extends Entity> Optional<T> anyEntityOfType(final EntityContainer container, final Class<T> type) {

	return container.streamEntitiesOfType(type).findAny();
    }

    /**
     * Checks to see if the entity has been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is of the type or
     *         {@code false} if it is not.
     */
    public static Predicate<Entity> isMarkedAsType(final Class<? extends Entity> type) {

	return i -> i.isMarkedAsType(type);
    }

    /**
     * Checks to see if the entity has not been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is not of the type or
     *         {@code false} if it is.
     */
    public static Predicate<Entity> notMarkedAsType(final Class<? extends Entity> type) {

	return isMarkedAsType(type).negate();
    }

    private Entities() {

	throw new UnsupportedOperationException();
    }
}
