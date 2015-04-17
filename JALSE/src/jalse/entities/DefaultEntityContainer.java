package jalse.entities;

import static jalse.entities.Entities.asType;
import static jalse.listeners.Listeners.newEntityListenerSet;
import static jalse.misc.JALSEExceptions.CANNOT_SELF_TRANSFER;
import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_EXPORT_NO_TRANSFER;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.attributes.AttributeContainer;
import jalse.listeners.EntityEvent;
import jalse.listeners.EntityListener;
import jalse.listeners.ListenerSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * An DefaultEntityContainer is a thread-safe implementation of {@link EntityContainer}. <br>
 * <br>
 *
 * DefaultEntityContainer can take a delegate container to supply to {@link EntityEvent}. Entity
 * updates will trigger these events using {@link EntityListener}.<br>
 * <br>
 *
 * By default DefaultEntityContainer will use {@link DefaultEntityFactory} with no delegate
 * container.
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityContainer implements EntityContainer {

    private final ConcurrentMap<UUID, Entity> entities;
    private final ListenerSet<EntityListener> listeners;
    private final EntityFactory factory;
    private final EntityContainer delegateContainer;
    private final Lock read;
    private final Lock write;

    /**
     * Creates an entity container with the default entity factory and no delegate container.
     *
     */
    public DefaultEntityContainer() {
	this(null, null);
    }

    /**
     * Creates an entity container with the supplied factory and delegate container.
     *
     * @param factory
     *            Entity creation/death factory.
     * @param delegateContainer
     *            Delegate container for events and entity creation.
     */
    public DefaultEntityContainer(final EntityFactory factory, final EntityContainer delegateContainer) {
	this.factory = factory != null ? factory : new DefaultEntityFactory();
	this.delegateContainer = delegateContainer != null ? delegateContainer : this;
	entities = new ConcurrentHashMap<>();
	listeners = newEntityListenerSet();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	Objects.requireNonNull(listener);

	read.lock();
	try {
	    return listeners.add(listener);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}

	if (!(obj instanceof DefaultEntityContainer)) {
	    return false;
	}

	final DefaultEntityContainer other = (DefaultEntityContainer) obj;
	return entities.equals(other.entities) && listeners.equals(other.listeners);
    }

    /**
     * Gets the delegate container for events and entity creation.
     *
     * @return Delegate container.
     */
    public EntityContainer getDelegateContainer() {
	return delegateContainer;
    }

    @Override
    public Entity getEntity(final UUID id) {
	Objects.requireNonNull(id);

	read.lock();
	try {
	    return entities.get(id);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public int getEntityCount() {
	return entities.size();
    }

    @Override
    public Set<UUID> getEntityIDs() {
	return Collections.unmodifiableSet(entities.keySet());
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	return Collections.unmodifiableSet(listeners);
    }

    /**
     * Gets entity factory for this set.
     *
     * @return Entity creation / death factory.
     */
    public EntityFactory getFactory() {
	return factory;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + entities.hashCode();
	result = prime * result + listeners.hashCode();
	return result;
    }

    @Override
    public void killEntities() {
	write.lock();
	try {
	    new HashSet<>(entities.keySet()).forEach(this::killEntity);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean killEntity(final UUID id) {
	Objects.requireNonNull(id);

	write.lock();
	try {
	    final Entity e = entities.get(id);
	    if (e == null || !factory.tryKillEntity(e)) {
		return false;
	    }

	    entities.remove(id);
	    listeners.getProxy().entityKilled(new EntityEvent(delegateContainer, e));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer sourceContainer) {
	return newEntity0(id, null, sourceContainer);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type, final AttributeContainer sourceContainer) {
	return asType(newEntity0(id, type, sourceContainer), type);
    }

    private Entity newEntity0(final UUID id, final Class<? extends Entity> type,
	    final AttributeContainer sourceContainer) {
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

	    e.addAll(sourceContainer);

	    listeners.getProxy().entityCreated(new EntityEvent(delegateContainer, e));

	    return e;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	if (Objects.equals(delegateContainer, Objects.requireNonNull(e))) {
	    throwRE(CANNOT_SELF_TRANSFER);
	}

	write.lock();
	try {

	    final UUID id = e.getID();
	    if (entities.containsKey(id)) {
		return false;
	    }

	    boolean imported = false;
	    if (!factory.tryTakeFromTree(e, delegateContainer)) {
		if (!factory.tryImportEntity(e, delegateContainer)) {
		    return false;
		}
		imported = true;
	    }

	    entities.put(id, e);
	    if (imported) { // Otherwise transfer is triggered.
		listeners.getProxy().entityReceived(new EntityEvent(delegateContainer, e));
	    }

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	write.lock();
	try {
	    return listeners.remove(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeEntityListeners() {
	read.lock();
	try {
	    listeners.clear();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Stream<Entity> streamEntities() {
	return entities.values().stream();
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
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

	    if (Objects.equals(e, destination)) {
		throwRE(CANNOT_SELF_TRANSFER);
	    }

	    boolean exported = false;
	    if (!factory.withinSameTree(delegateContainer, destination)) {
		factory.exportEntity(e);
		exported = true;
	    }

	    if (!destination.receiveEntity(e)) {
		if (exported) {
		    throwRE(ENTITY_EXPORT_NO_TRANSFER);
		}
		return false;
	    }

	    entities.remove(id);
	    listeners.getProxy().entityTransferred(new EntityEvent(delegateContainer, e, destination));

	    return true;
	} finally {
	    write.unlock();
	}
    }
}
