package jalse.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jalse.attributes.AttributeListener;
import jalse.attributes.NamedAttributeType;
import jalse.entities.EntityVisitor.EntityVisitResult;

/**
 * A utility for {@link Entity} related functionality (specifically around entity types).<br>
 * <br>
 * An Entity type allows entities to be used in an Object-Oriented way (get/set). It does this by
 * providing a number of annotations that specify to the proxy its behaviour.<br>
 * <br>
 * Entity types are soft types - any entity can be 'cast' ({@link #asType(Entity, Class)}) to
 * another entity type even if it does not have the defining data. This does not set the entity as
 * this type but entities can be marked as a type manually for easy filtering/processing (
 * {@link Entity#markAsType(Class)}). When creating an entity and supplying an entity type (like
 * {@link EntityContainer#newEntity(Class)}) this automatically marks the entity as the supplied
 * type - in fact all of the types in the entity type inheritance tree are added too (removable). An
 * entity can be marked as multiple entity types so filtering this way can become very useful for
 * processing similar entities. <br>
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 * @see #walkEntities(EntityContainer)
 * @see #walkEntityTree(EntityContainer, EntityVisitor)
 */
public final class Entities {

    /**
     * An empty EntityContainer.
     */
    public static EntityContainer EMPTY_ENTITYCONTAINER = new UnmodifiableDelegateEntityContainer(null);

    /**
     * An empty EntityFactory.
     */
    public static EntityFactory EMPTY_ENTITYFACTORY = new UnmodifiableDelegateEntityFactory(null);

    private static AtomicReference<EntityProxyFactory> proxyFactory = new AtomicReference<>(
	    new DefaultEntityProxyFactory());

    @SuppressWarnings("unchecked")
    private static void addDirectTypeAncestors(final Set<Class<? extends Entity>> ancestry, final Class<?> type) {
	for (final Class<?> t : type.getInterfaces()) {
	    if (!t.equals(Entity.class) && ancestry.add((Class<? extends Entity>) t)) {
		addDirectTypeAncestors(ancestry, t);
	    }
	}
    }

    /**
     * Gets any entity within the container.
     *
     * @param container
     *            Entity container.
     *
     * @return Gets an Optional of the resulting entity or an empty Optional if it was not found.
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
     * @return Gets an Optional of the resulting entity or an empty Optional if it was not found.
     *
     * @see Entity#markAsType(Class)
     */
    public static <T extends Entity> Optional<T> anyEntityOfType(final EntityContainer container, final Class<T> type) {
	return container.streamEntitiesOfType(type).findAny();
    }

    /**
     * Wraps an Entity as the supplied Entity type. This is a convenience method for
     * {@link EntityProxyFactory#proxyOfEntity(Entity, Class)}.
     *
     * @param entity
     *            Entity to wrap.
     * @param type
     *            Entity type to wrap to.
     * @return The wrapped Entity.
     *
     * @see #getProxyFactory()
     */
    public static <T extends Entity> T asType(final Entity entity, final Class<T> type) {
	return getProxyFactory().proxyOfEntity(entity, type);
    }

    /**
     * Creates an immutable empty entity container.
     *
     * @return Empty entity container.
     */
    public static EntityContainer emptyEntityContainer() {
	return EMPTY_ENTITYCONTAINER;
    }

    /**
     * Creates an immutable empty entity factory.
     *
     * @return Empty entity factory.
     */
    public static EntityFactory emptyEntityFactory() {
	return EMPTY_ENTITYFACTORY;
    }

    /**
     * Walks through the entity tree looking for an entity.
     *
     * @param container
     *            Entity container.
     * @param id
     *            Entity ID to look for.
     * @return Whether the entity was found.
     *
     * @see #walkEntityTree(EntityContainer, EntityVisitor)
     */
    public static boolean findEntityRecursively(final EntityContainer container, final UUID id) {
	final AtomicBoolean found = new AtomicBoolean();

	walkEntityTree(container, e -> {
	    if (id.equals(e.getID())) {
		found.set(true);
		return EntityVisitResult.EXIT;
	    } else {
		return EntityVisitResult.CONTINUE;
	    }
	});

	return found.get();
    }

    /**
     * Gets the total entity count (recursive).
     *
     * @param container
     *            Entity container.
     *
     * @return Total entity count.
     *
     * @see #walkEntityTree(EntityContainer, EntityVisitor)
     */
    public static int getEntityCountRecursively(final EntityContainer container) {
	final AtomicInteger result = new AtomicInteger();

	walkEntityTree(container, e -> {
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
     *
     * @see #walkEntityTree(EntityContainer, EntityVisitor)
     */
    public static Set<UUID> getEntityIDsRecursively(final EntityContainer container) {
	final Set<UUID> result = new HashSet<>();

	walkEntityTree(container,
		e -> result.add(e.getID()) ? EntityVisitResult.CONTINUE : EntityVisitResult.IGNORE_CHILDREN);

	return result;
    }

    /**
     * Gets the highest level parent of this container.
     *
     * @param container
     *            Container to get parent for.
     * @return Highest level parent (or this container if it has no parent).
     */
    public static EntityContainer getHighestParent(final EntityContainer container) {
	Objects.requireNonNull(container);

	if (container instanceof Entity) {
	    final EntityContainer parent = ((Entity) container).getContainer();
	    if (parent != null) {
		return getHighestParent(parent);
	    }
	}
	return container;
    }

    public static EntityProxyFactory getProxyFactory() {
	return proxyFactory.get();
    }

    /**
     * Gets all ancestors for the specified descendant type (not including {@link Entity}).
     *
     * @param type
     *            Descendant type.
     * @return All ancestors or an empty set if its only ancestor is {@link Entity}.
     *
     * @throws IllegalArgumentException
     *             If the Entity type is invalid
     *
     * @see #validateType(Class)
     */
    public static Set<Class<? extends Entity>> getTypeAncestry(final Class<? extends Entity> type) {
	validateType(type);

	final Set<Class<? extends Entity>> ancestry = new HashSet<>();
	addDirectTypeAncestors(ancestry, type);
	return ancestry;
    }

    /**
     * Checks whether the type is an entity subtype.
     *
     * @param type
     *            Type to check.
     * @return Whether the type is a descendant of entity.
     */
    public static boolean isEntityOrSubtype(final Class<?> type) {
	return Entity.class.isAssignableFrom(type);
    }

    /**
     * Checks whether the type is an entity subtype.
     *
     * @param type
     *            Type to check.
     * @return Whether the type is a descendant of entity.
     */
    public static boolean isEntitySubtype(final Class<?> type) {
	return !Entity.class.equals(type) && Entity.class.isAssignableFrom(type);
    }

    /**
     * Checks to see if the entity has been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is of the type or {@code false} if it is not.
     */
    public static Predicate<Entity> isMarkedAsType(final Class<? extends Entity> type) {
	return i -> i.isMarkedAsType(type);
    }

    /**
     * Checks if the specified type is equal to or a descendant from the specified ancestor type.
     *
     * @param descendant
     *            Descendant type.
     * @param ancestor
     *            Ancestor type.
     * @return Whether the descendant is equal or descended from the ancestor type.
     */
    public static boolean isOrSubtype(final Class<? extends Entity> descendant,
	    final Class<? extends Entity> ancestor) {
	return ancestor.isAssignableFrom(descendant);
    }

    /**
     * Checks if the specified type is a descendant from the specified ancestor type.
     *
     * @param descendant
     *            Descendant type.
     * @param ancestor
     *            Ancestor type.
     * @return Whether the descendant is descended from the ancestor type.
     */
    public static boolean isSubtype(final Class<? extends Entity> descendant, final Class<? extends Entity> ancestor) {
	return !ancestor.equals(descendant) && ancestor.isAssignableFrom(descendant);
    }

    /**
     * Creates an recursive entity listener for named attribute type and the supplied attribute
     * listener supplier with Integer.MAX_VALUE recursion limit.
     *
     * @param namedType
     *            Named attribute type being listened for by supplier's listeners.
     * @param supplier
     *            Supplier of the attribute listener to be added to created entities.
     * @return Recursive attribute listener for named type and supplier.
     */
    public static <T> EntityListener newRecursiveAttributeListener(final NamedAttributeType<T> namedType,
	    final Supplier<AttributeListener<T>> supplier) {
	return newRecursiveAttributeListener(namedType, supplier, Integer.MAX_VALUE);
    }

    /**
     * Creates an recursive entity listener for named attribute type and the supplied attribute
     * listener supplier with specified recursion limit.
     *
     * @param namedType
     *            Named attribute type being listened for by supplier's listeners.
     * @param supplier
     *            Supplier of the attribute listener to be added to created entities.
     * @param depth
     *            The recursion limit of the listener.
     * @return Recursive attribute listener for named type and supplier.
     */
    public static <T> EntityListener newRecursiveAttributeListener(final NamedAttributeType<T> namedType,
	    final Supplier<AttributeListener<T>> supplier, final int depth) {
	return new RecursiveAttributeListener<>(namedType, supplier, depth);
    }

    /**
     * Creates a recursive entity listener for the supplied entity listener supplier with
     * Integer.MAX_VALUE recursion limit.
     *
     * @param supplier
     *            Supplier of the entity listener to be added to created entities.
     * @return Recursive entity listener with Integer.MAX_VALUE recursion limit.
     */
    public static EntityListener newRecursiveEntityListener(final Supplier<EntityListener> supplier) {
	return newRecursiveEntityListener(supplier, Integer.MAX_VALUE);
    }

    /**
     * Creates a recursive entity listener for the supplied entity listener supplier and specified
     * recursion limit.
     *
     * @param supplier
     *            Supplier of the entity listener to be added to created entities.
     * @param depth
     *            The recursion limit of the listener.
     * @return Recursive entity listener with specified recursion limit.
     */
    public static EntityListener newRecursiveEntityListener(final Supplier<EntityListener> supplier, final int depth) {
	return new RecursiveEntityListener(supplier, depth);
    }

    /**
     * Checks to see if the entity has not been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is not of the type or {@code false} if it is.
     */
    public static Predicate<Entity> notMarkedAsType(final Class<? extends Entity> type) {
	return isMarkedAsType(type).negate();
    }

    /**
     * Gets a random entity from the container (if there is one).
     *
     * @param container
     *            Source container.
     * @return Random entity (if found).
     */
    public static Optional<Entity> randomEntity(final EntityContainer container) {
	return randomEntity0(container.streamEntities(), container.getEntityCount());
    }

    private static <T extends Entity> Optional<T> randomEntity0(final Stream<T> entities, final int size) {
	return size > 0 ? entities.skip(ThreadLocalRandom.current().nextInt(size)).findFirst() : Optional.empty();
    }

    /**
     * Gets a random entity from the container of the given type (if there is one).
     *
     * @param container
     *            Source container.
     * @param type
     *            Entity type.
     * @return Random entity (if found).
     */
    public static <T extends Entity> Optional<T> randomEntityOfType(final EntityContainer container,
	    final Class<T> type) {
	final Set<T> entities = container.getEntitiesOfType(type);
	final int size = entities.size();
	return randomEntity0(entities.stream(), size);
    }

    public static void setProxyFactory(final EntityProxyFactory newFactory) {
	proxyFactory.set(Objects.requireNonNull(newFactory));
    }

    /**
     * Creates an immutable read-only delegate entity container for the supplied container.
     *
     * @param container
     *            Container to delegate for.
     * @return Immutable entity container.
     */
    public static EntityContainer unmodifiableEntityContainer(final EntityContainer container) {
	return new UnmodifiableDelegateEntityContainer(Objects.requireNonNull(container));
    }

    /**
     * Creates an immutable read-only delegate entity factory for the supplied factory.
     *
     * @param factory
     *            Factory to delegate for.
     * @return Immutable entity factory.
     */
    public static EntityFactory unmodifiableEntityFactory(final EntityFactory factory) {
	return new UnmodifiableDelegateEntityFactory(Objects.requireNonNull(factory));
    }

    /**
     * Validates the entity type. This is a convenience method for
     * {@link EntityProxyFactory#validateType(Class)}.
     *
     * @param type
     *            Type to validate.
     *
     * @see #getProxyFactory()
     */
    public static void validateType(final Class<? extends Entity> type) {
	getProxyFactory().validateType(type);
    }

    /**
     * A lazy-walked stream of entities (recursive and breadth-first). The entire stream will not be
     * loaded until it is iterated through.
     *
     * This is equivalent to {@code walkEntities(container, Integer.MAX_VALUE)}
     *
     * @param container
     *            Entity container.
     * @return Lazy-walked recursive stream of entities.
     */
    public static Stream<Entity> walkEntities(final EntityContainer container) {
	return walkEntities(container, Integer.MAX_VALUE);
    }

    /**
     * A lazy-walked stream of entities (recursive and breadth-first). The entire stream will not be
     * loaded until it is iterated through.
     *
     * @param container
     *            Entity container.
     * @param maxDepth
     *            Maximum depth of the walk.
     * @return Lazy-walked recursive stream of entities.
     */
    public static Stream<Entity> walkEntities(final EntityContainer container, final int maxDepth) {
	final EntityTreeWalker walker = new EntityTreeWalker(container, maxDepth, e -> EntityVisitResult.CONTINUE);
	final Iterator<Entity> iterator = new Iterator<Entity>() {

	    @Override
	    public boolean hasNext() {
		return walker.isWalking();
	    }

	    @Override
	    public Entity next() {
		return walker.walk();
	    }
	};

	final int characteristics = Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.DISTINCT;
	return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
    }

    /**
     * Walks through all entities (recursive and breadth-first). Walking can be stopped or filtered
     * based on the visit result returned.
     *
     * This is equivalent to {@code walkEntityTree(container, Integer.MAX_VALUE, visitor)}
     *
     * @param container
     *            Entity container.
     * @param visitor
     *            Entity visitor.
     *
     * @see EntityVisitor
     */
    public static void walkEntityTree(final EntityContainer container, final EntityVisitor visitor) {
	walkEntityTree(container, Integer.MAX_VALUE, visitor);
    }

    /**
     * Walks through all entities (recursive and breadth-first). Walking can be stopped or filtered
     * based on the visit result returned.
     *
     * @param container
     *            Entity container.
     * @param maxDepth
     *            Maximum depth of the walk.
     * @param visitor
     *            Entity visitor.
     *
     * @see EntityVisitor
     */
    public static void walkEntityTree(final EntityContainer container, final int maxDepth,
	    final EntityVisitor visitor) {
	final EntityTreeWalker walker = new EntityTreeWalker(container, maxDepth, visitor);
	while (walker.isWalking()) {
	    walker.walk();
	}
    }

    /**
     * Checks to see if an container is in the same tree as the other container (checking highest
     * parent container).
     *
     * @param one
     *            Container to check.
     * @param two
     *            Container to check.
     * @return Whether the container is within the same tree as the other container.
     *
     * @see #getHighestParent(EntityContainer)
     */
    public static boolean withinSameTree(final EntityContainer one, final EntityContainer two) {
	return Objects.equals(getHighestParent(one), getHighestParent(two));
    }

    private Entities() {
	throw new UnsupportedOperationException();
    }
}
