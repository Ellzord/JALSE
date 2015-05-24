package jalse.entities;

import static jalse.entities.Entities.asType;
import static jalse.listeners.Listeners.newEntityContainerListenerSet;
import static jalse.misc.JALSEExceptions.CANNOT_SELF_TRANSFER;
import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_EXPORT_NO_TRANSFER;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.attributes.AttributeContainer;
import jalse.listeners.EntityContainerEvent;
import jalse.listeners.EntityContainerListener;
import jalse.listeners.ListenerSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * An DefaultEntityContainer is a thread-safe implementation of {@link EntityContainer}. <br>
 * <br>
 *
 * DefaultEntityContainer can take a delegate container to supply to {@link EntityContainerEvent}.
 * Entity updates will trigger these events using {@link EntityContainerListener}.<br>
 * <br>
 *
 * By default DefaultEntityContainer will use {@link DefaultEntityFactory} with no delegate
 * container.
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityContainer implements EntityContainer {

    private final Map<UUID, Entity> entities;
    private final ListenerSet<EntityContainerListener> listeners;
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
	entities = new HashMap<>();
	listeners = newEntityContainerListenerSet();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public boolean addEntityContainerListener(final EntityContainerListener listener) {
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    return listeners.add(listener);
	} finally {
	    write.unlock();
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
    public Set<? extends EntityContainerListener> getEntityContainerListeners() {
	read.lock();
	try {
	    return new HashSet<>(listeners);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public int getEntityCount() {
	read.lock();
	try {
	    return entities.size();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<UUID> getEntityIDs() {
	read.lock();
	try {
	    return new HashSet<>(entities.keySet());
	} finally {
	    read.unlock();
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
	    listeners.getProxy().entityKilled(new EntityContainerEvent(delegateContainer, e));

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

	    listeners.getProxy().entityCreated(new EntityContainerEvent(delegateContainer, e));

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
		listeners.getProxy().entityReceived(new EntityContainerEvent(delegateContainer, e));
	    }

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean removeEntityContainerListener(final EntityContainerListener listener) {
	write.lock();
	try {
	    return listeners.remove(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeEntityContainerListeners() {
	write.lock();
	try {
	    listeners.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public Stream<Entity> streamEntities() {
	read.lock();
	try {
	    return new HashSet<>(entities.values()).stream();
	} finally {
	    read.unlock();
	}
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
	    listeners.getProxy().entityTransferred(new EntityContainerEvent(delegateContainer, e, destination));

	    return true;
	} finally {
	    write.unlock();
	}
    }
}
