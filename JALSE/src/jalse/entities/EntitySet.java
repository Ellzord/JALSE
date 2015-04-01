package jalse.entities;

import static jalse.entities.Entities.asType;
import static jalse.entities.Entities.toEntityContainer;
import static jalse.listeners.Listeners.newEntityListenerSet;
import static jalse.misc.JALSEExceptions.CANNOT_SELF_TRANSFER;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

    private final ConcurrentMap<UUID, Entity> entities;
    private final ListenerSet<EntityListener> entityListeners;
    private final EntityFactory factory;
    private final EntityContainer delegateContainer;
    private final Lock read;
    private final Lock write;

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
	entityListeners = newEntityListenerSet();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
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
	Objects.requireNonNull(listener);

	read.lock();
	try {
	    return entityListeners.add(listener);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public void clear() {
	write.lock();
	try {
	    new HashSet<>(entities.keySet()).forEach(this::killEntity);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean contains(final Object o) {
	Objects.requireNonNull(o);

	if (!(o instanceof Entity)) {
	    throw new IllegalArgumentException();
	}
	return getEntity(((Entity) o).getID()) != null;
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
	Objects.requireNonNull(id);

	read.lock();
	try {
	    return entities.get(id);
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets the IDs of all the entities within the container.
     *
     * @return Set of all entity identifiers.
     */
    public Set<UUID> getEntityIDs() {
	return Collections.unmodifiableSet(entities.keySet());
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

    @Override
    public boolean isEmpty() {
	return entities.isEmpty();
    }

    @Override
    public Iterator<Entity> iterator() {
	return entities.values().iterator();
    }

    /**
     * Kills the specified entity.
     *
     * @param id
     *            Entity ID.
     * @return Whether the entity was alive.
     * @see EntityFactory#tryKillEntity(Entity)
     */
    public boolean killEntity(final UUID id) {
	Objects.requireNonNull(id);

	write.lock();
	try {
	    final Entity e = entities.get(id);
	    if (e == null || !factory.tryKillEntity(e)) {
		return false;
	    }

	    entities.remove(id);
	    entityListeners.getProxy().entityKilled(new EntityEvent(delegateContainer, e));

	    return true;
	} finally {
	    write.unlock();
	}
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
	Objects.requireNonNull(id);

	write.lock();
	try {
	    Entity e = entities.get(id);
	    if (e != null) {
		throwRE(ENTITY_ALREADY_ASSOCIATED);
	    }

	    e = factory.newEntity(id, delegateContainer);
	    entities.put(id, e);

	    if (type != null) {
		e.markAsType(type);
	    }

	    entityListeners.getProxy().entityCreated(new EntityEvent(delegateContainer, e));

	    return e;
	} finally {
	    write.unlock();
	}
    }

    /**
     * Receives an entity (from a transfer).
     *
     * @param e
     *            Entity to receive.
     * @return Whether the entity was received.
     *
     * @see #transferOrExport(UUID, EntityContainer)
     */
    public boolean receive(final Entity e) {
	Objects.requireNonNull(e);

	write.lock();
	try {
	    final UUID id = e.getID();
	    if (entities.containsKey(id) || !factory.tryImportEntity(e, delegateContainer)) {
		return false;
	    }

	    entities.put(id, e);
	    entityListeners.getProxy().entityReceived(new EntityEvent(delegateContainer, e));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    /**
     * Receives an entity from within the tree (so does not need to import).
     *
     * @param e
     *            Entity to receive.
     * @return Whether the entity was added.
     *
     * @see EntityFactory#tryMoveWithinTree(Entity, EntityContainer)
     */
    public boolean receiveFromTree(final Entity e) {
	Objects.requireNonNull(e);

	write.lock();
	try {
	    final UUID id = e.getID();

	    if (entities.containsKey(id)) {
		return false;
	    }

	    entities.put(id, e);
	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean remove(final Object o) {
	Objects.requireNonNull(o);

	if (!(o instanceof Entity)) {
	    throw new IllegalArgumentException();
	}
	return killEntity(((Entity) o).getID());
    }

    /**
     * Removes all listeners for entities.
     */
    public void removeAllListeners() {
	read.lock();
	try {
	    entityListeners.clear();
	} finally {
	    read.unlock();
	}
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
	write.lock();
	try {
	    return entityListeners.remove(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public int size() {
	return entities.size();
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
    public boolean transferOrExport(final UUID id, final EntityContainer destination) {
	Objects.requireNonNull(id);

	if (Objects.equals(delegateContainer, Objects.requireNonNull(destination))) {
	    throwRE(CANNOT_SELF_TRANSFER);
	}

	write.lock();
	try {
	    final Entity e = entities.get(id);
	    if (e == null) {
		return false;
	    }

	    if (!factory.tryMoveWithinTree(e, destination)) {
		factory.exportEntity(e);

		if (!destination.receiveEntity(e)) {
		    throwRE(ENTITY_EXPORT_NO_TRANSFER);
		}
	    }
	    entities.remove(id);
	    entityListeners.getProxy().entityTransferred(new EntityEvent(delegateContainer, e, destination));

	    return true;
	} finally {
	    write.unlock();
	}
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
    public boolean transferOrExport(final UUID id, final EntitySet destination) {
	return transferOrExport(id, toEntityContainer(destination));
    }
}
