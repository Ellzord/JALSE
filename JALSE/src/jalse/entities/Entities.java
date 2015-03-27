package jalse.entities;

import jalse.entities.EntityVisitor.EntityVisitResult;
import jalse.misc.JALSEExceptions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A utility for {@link Entity} related functionality (specifically around entity types).<br>
 * <br>
 * An Entity type allows entities to be used in an Object-Oriented way (get/set). It does this by
 * providing a number of annotations that specify to the proxy its behaviour. Entity types also
 * support default methods allowing some logic to be defined.<br>
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
 * @see EntityProxies
 * @see #walkEntities(EntityContainer)
 * @see #walkEntityTree(EntityContainer, EntityVisitor)
 */
public final class Entities {

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
     * {@link EntityProxies#proxyOfEntity(Entity, Class)}.
     *
     * @param entity
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
     * @see EntityProxies
     * @see JALSEExceptions#INVALID_ENTITY_TYPE
     */
    public static <T extends Entity> T asType(final Entity entity, final Class<T> type) {
	return EntityProxies.proxyOfEntity(entity, type);
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

	walkEntityTree(container, e -> result.add(e.getID()) ? EntityVisitResult.CONTINUE
		: EntityVisitResult.IGNORE_CHILDREN);

	return result;
    }

    /**
     * Gets the highest level parent of this entity.
     *
     * @param e
     *            Entity to get parent for.
     * @return Highest level parent (or this entity if it has no parent.
     */
    public static EntityContainer getHighestParent(final Entity e) {
	final EntityContainer container = e.getOrNullContainer();

	if (container instanceof Entity) {
	    return getHighestParent((Entity) container);
	} else {
	    return container != null ? container : e;
	}
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
     * @see JALSEExceptions#INVALID_ENTITY_TYPE
     */
    public static Set<Class<? extends Entity>> getTypeAncestry(final Class<? extends Entity> type) {
	EntityProxies.validateEntityType(type);

	final Set<Class<? extends Entity>> ancestry = new HashSet<>();
	addDirectTypeAncestors(ancestry, type);
	return ancestry;
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
    public static boolean isOrTypeDescendant(final Class<? extends Entity> descendant,
	    final Class<? extends Entity> ancestor) {
	return ancestor.isAssignableFrom(descendant);
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
     * Wraps the entity set as an entity container.
     *
     * @param entities
     *            Entity set.
     * @return Entity container.
     */
    public static EntityContainer toEntityContainer(final EntitySet entities) {
	return new EntitySetContainer(entities);
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
	return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT), false);
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
    public static void walkEntityTree(final EntityContainer container, final int maxDepth, final EntityVisitor visitor) {
	final EntityTreeWalker walker = new EntityTreeWalker(container, maxDepth, visitor);
	while (walker.isWalking()) {
	    walker.walk();
	}
    }

    /**
     * Checks to see if an entity is in the same tree as the container.
     *
     * @param e
     *            Entity to check.
     * @param container
     *            Container to check.
     * @return Whether the entity is within the same tree as the container.
     */
    public static boolean withinSameTree(final Entity e, final EntityContainer container) {
	return Objects.equals(getHighestParent(e), container instanceof Entity ? getHighestParent((Entity) container)
		: container);
    }

    private Entities() {
	throw new UnsupportedOperationException();
    }
}
