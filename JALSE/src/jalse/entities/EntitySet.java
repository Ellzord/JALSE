package jalse.entities;

import static jalse.entities.Entities.asType;
import static jalse.entities.Entities.toEntityContainer;
import static jalse.listeners.Listeners.createEntityListenerSet;
import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_EXPORT_NO_TRANSFER;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.listeners.EntityEvent;
import jalse.listeners.EntityListener;
import jalse.listeners.ListenerSet;
import jalse.misc.JALSEExceptions;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An EntitySet is a thread-safe {@link Set} implementation for {@link Entity}. An EntitySet follows
 * the same pattern defined in {@link EntityContainer} but offers a collections based implementation
 * (so it may be used more generically).<br>
 * <br>
 *
 * EntitySet can take a delegate EntityContainer to supply to {@link EntityEvent}. Entity updates
 * will trigger these events using {@link EntityListener}.<br>
 * <br>
 *
 * By default EntitySet will use {@link DefaultEntityFactory} with no delegate container.
 *
 * @author Elliot Ford
 *
 */
public class EntitySet extends AbstractSet<Entity> {

    private final Map<UUID, Entity> entities;
    private final ListenerSet<EntityListener> entityListeners;
    private final EntityFactory factory;
    private final EntityContainer delegateContainer;
    private final StampedLock lock;

    /**
     * Creates an entity set with the default entity factory and no delegate container.
     *
     */
    public EntitySet() {
	this(null, null);
    }

    /**
     * Creates an entity set with the supplied factory and delegate container.
     *
     * @param factory
     *            Entity creation/death factory.
     * @param delegateContainer
     *            Delegate container for events and entity creation.
     */
    public EntitySet(final EntityFactory factory, final EntityContainer delegateContainer) {
	this.factory = factory != null ? factory : new DefaultEntityFactory();
	this.delegateContainer = delegateContainer != null ? delegateContainer : Entities.toEntityContainer(this);
	entities = new ConcurrentHashMap<>();
	entityListeners = createEntityListenerSet();
	lock = new StampedLock();
    }

    /**
     * Adds a listener for entities.
     *
     * @param listener
     *            Listener to add.
     *
     * @return {@code true} if container did not already contain this listener.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#add(Object)
     *
     */
    public boolean addListener(final EntityListener listener) {
	return entityListeners.add(listener);
    }

    @Override
    public void clear() {
	final Set<UUID> toKill = new HashSet<>();

	final long stamp = lock.readLock();
	try {
	    toKill.addAll(entities.keySet());
	} finally {
	    lock.unlockRead(stamp);
	}

	toKill.forEach(this::killEntity);
    }

    @Override
    public boolean contains(final Object o) {
	if (!(o instanceof Entity)) {
	    throw new IllegalArgumentException();
	}
	return hasEntity(((Entity) o).getID());
    }

    /**
     * Gets all the entities as the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities as the type.
     *
     * @see Entity#asType(Class)
     */
    public <T extends Entity> Set<T> getAsType(final Class<T> type) {
	return streamAsType(type).collect(Collectors.toSet());
    }

    /**
     * Gets the delegate container for events and entity creation.
     *
     * @return Delegate container.
     */
    public EntityContainer getDelegateContainer() {
	return delegateContainer;
    }

    /**
     * Gets the entity with the specified ID.
     *
     * @param id
     *            Unique ID of the entity.
     * @return The entity matching the supplied id or null if none found.
     */
    public Entity getEntity(final UUID id) {
	final long stamp = lock.readLock();
	try {
	    return entities.get(Objects.requireNonNull(id));
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets the IDs of all the entities within the container.
     *
     * @return Set of all entity identifiers.
     */
    public Set<UUID> getEntityIDs() {
	final long stamp = lock.readLock();
	try {
	    return Collections.unmodifiableSet(new HashSet<>(entities.keySet()));
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets entity factory for this set.
     *
     * @return Entity creation / death factory.
     */
    public EntityFactory getFactory() {
	return factory;
    }

    /**
     * Gets all the entity listeners.
     *
     * @return All the entity listeners.
     */
    public Set<? extends EntityListener> getListeners() {
	return Collections.unmodifiableSet(entityListeners);
    }

    /**
     * Gets all the entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     */
    public <T extends Entity> Set<T> getOfType(final Class<T> type) {
	return streamOfType(type).collect(Collectors.toSet());
    }

    /**
     * Checks whether the entity is contained.
     *
     * @param id
     *            Entity ID.
     * @return Whether the entity was found.
     */
    public boolean hasEntity(final UUID id) {
	final long stamp = lock.readLock();
	try {
	    return entities.containsKey(Objects.requireNonNull(id));
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    @Override
    public boolean isEmpty() {
	final long stamp = lock.readLock();
	try {
	    return entities.isEmpty();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    @Override
    public Iterator<Entity> iterator() {
	final long stamp = lock.readLock();
	try {
	    return entities.values().iterator();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Kills the specified entity.
     *
     * @param id
     *            Entity ID.
     * @return Whether the entity was alive.
     * @see EntityFactory#killEntity(Entity)
     */
    public boolean killEntity(final UUID id) {
	Objects.requireNonNull(id);

	Entity e;
	final long stamp = lock.writeLock();
	try {
	    e = entities.get(id);
	    if (e == null || !factory.killEntity(e)) { // Only kill if allowed
		return false;
	    }

	    entities.remove(id);
	} finally {
	    lock.unlockWrite(stamp);
	}

	entityListeners.getProxy().entityKilled(new EntityEvent(delegateContainer, e));

	return true;
    }

    /**
     * Creates a new entity with a random ID.
     *
     * @return The newly created entity's ID.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see EntityFactory#newEntity(UUID, EntityContainer)
     */
    public Entity newEntity() {
	return newEntity(UUID.randomUUID());
    }

    /**
     * Creates a new entity with a random ID. This entity is marked as the specified entity type and
     * then wrapped to it.
     *
     * @param type
     *            Entity type.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     * @see EntityFactory#newEntity(UUID, EntityContainer)
     */
    public <T extends Entity> T newEntity(final Class<T> type) {
	return newEntity(UUID.randomUUID(), type);
    }

    /**
     * Creates new entity with the specified ID.
     *
     * @param id
     *            Entity ID.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     *
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see JALSEExceptions#ENTITY_ALREADY_ASSOCIATED
     * @see EntityFactory#newEntity(UUID, EntityContainer)
     */
    public Entity newEntity(final UUID id) {
	return newEntity0(Objects.requireNonNull(id), null);
    }

    /**
     * Creates new entity with the specified ID. This entity is marked as the specified entity type
     * and then wrapped to it.
     *
     *
     * @param id
     *            Entity ID.
     * @param type
     *            Entity type.
     * @return The newly created entity.
     * @throws IllegalStateException
     *             If the entity limit has been reached.
     * @throws IllegalArgumentException
     *             If the entity ID is already assigned.
     *
     * @see JALSEExceptions#ENTITY_LIMIT_REACHED
     * @see JALSEExceptions#ENTITY_ALREADY_ASSOCIATED
     * @see Entity#markAsType(Class)
     * @see Entities#asType(Entity, Class)
     * @see EntityFactory#newEntity(UUID, EntityContainer)
     */
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	return asType(newEntity0(Objects.requireNonNull(id), type), type);
    }

    private Entity newEntity0(final UUID id, final Class<? extends Entity> type) {
	Entity e;
	final long stamp = lock.writeLock();
	try {
	    if (entities.containsKey(id)) {
		throwRE(ENTITY_ALREADY_ASSOCIATED);
	    }

	    e = factory.newEntity(id, delegateContainer);
	    entities.put(e.getID(), e);

	    if (type != null) {
		e.markAsType(type); // Must be done before listeners
	    }
	} finally {
	    lock.unlockWrite(stamp);
	}

	entityListeners.getProxy().entityCreated(new EntityEvent(delegateContainer, e));

	return e;
    }

    /**
     * Receives an entity (from a transfer).
     *
     * @param e
     *            Entity to receive.
     * @return Whether the entity was received.
     *
     * @see #transfer(UUID, EntityContainer)
     */
    public boolean receive(final Entity e) {
	final long stamp = lock.writeLock();
	try {
	    if (entities.containsKey(e.getID()) || !factory.importEntity(e, delegateContainer)) {
		return false;
	    }

	    entities.put(e.getID(), e);
	} finally {
	    lock.unlockWrite(stamp);
	}

	entityListeners.getProxy().entityReceived(new EntityEvent(delegateContainer, e));

	return true;
    }

    @Override
    public boolean remove(final Object o) {
	if (!(o instanceof Entity)) {
	    throw new IllegalArgumentException();
	}
	return killEntity(((Entity) o).getID());
    }

    /**
     * Removes all listeners for entities.
     */
    public void removeAllListeners() {
	entityListeners.clear();
    }

    /**
     * Removes a entity listener.
     *
     * @param listener
     *            Listener to remove.
     *
     * @return {@code true} if the listener was removed.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#remove(Object)
     *
     */
    public boolean removeListener(final EntityListener listener) {
	return entityListeners.remove(listener);
    }

    @Override
    public int size() {
	final long stamp = lock.readLock();
	try {
	    return entities.size();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets a stream of as the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Stream of entities as the type.
     *
     * @see Entity#asType(Class)
     */
    public <T extends Entity> Stream<T> streamAsType(final Class<T> type) {
	return stream().map(e -> asType(e, type));
    }

    /**
     * Gets a stream of entities marked with the specified type.
     *
     * @param type
     *            Entity type to check for.
     * @return Set of entities marked with the type.
     *
     * @see Entity#isMarkedAsType(Class)
     * @see Entity#asType(Class)
     */
    public <T extends Entity> Stream<T> streamOfType(final Class<T> type) {
	return stream().filter(e -> e.isMarkedAsType(type)).map(e -> asType(e, type));
    }

    /**
     * Transfers the entity to the supplied destination container.
     *
     * @param id
     *            Entity ID.
     * @param destination
     *            Target container.
     * @return Whether the entity was transfered.
     *
     * @see #receive(Entity)
     */
    public boolean transfer(final UUID id, final EntityContainer destination) {
	Objects.requireNonNull(id);
	if (Objects.equals(delegateContainer, Objects.requireNonNull(destination))) {
	    throw new IllegalArgumentException("Cannot transfer to the same container");
	}

	Entity e;

	final long stamp = lock.writeLock();
	try {
	    e = entities.get(id);
	    if (e == null || !factory.exportEntity(e)) {
		return false;
	    }

	    entities.remove(e.getID());
	} finally {
	    lock.unlockWrite(stamp);
	}

	if (!destination.receiveEntity(e)) {
	    throwRE(ENTITY_EXPORT_NO_TRANSFER);
	}

	entityListeners.getProxy().entityTransferred(new EntityEvent(delegateContainer, e, destination));

	return true;
    }

    /**
     * Transfers the entity to the supplied destination set.
     *
     * @param id
     *            Entity ID.
     * @param destination
     *            Target set.
     * @return Whether the entity was transfered.
     *
     * @see #receive(Entity)
     */
    public boolean transfer(final UUID id, final EntitySet destination) {
	return transfer(id, toEntityContainer(destination));
    }
}
