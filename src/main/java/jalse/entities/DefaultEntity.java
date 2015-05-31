package jalse.entities;

import static jalse.entities.Entities.getTypeAncestry;
import static jalse.entities.Entities.isOrSubtype;
import static jalse.entities.Entities.isSubtype;
import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.ActionEngine;
import jalse.actions.DefaultActionScheduler;
import jalse.actions.MutableActionContext;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeListener;
import jalse.attributes.AttributeType;
import jalse.attributes.DefaultAttributeContainer;
import jalse.misc.AbstractIdentifiable;
import jalse.misc.Identifiable;
import jalse.misc.ListenerSet;
import jalse.tags.Parent;
import jalse.tags.Tag;
import jalse.tags.TagTypeSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple yet fully featured {@link Entity} implementation.<br>
 * <br>
 * This entity can be marked as alive ({@link #markAsAlive()}) or dead ({@link #markAsDead()}).
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityFactory
 * @see DefaultEntityContainer
 * @see DefaultAttributeContainer
 * @see DefaultActionScheduler
 * @see TagTypeSet
 *
 */
public class DefaultEntity extends AbstractIdentifiable implements Entity {

    /**
     * Parent entity container.
     */
    protected EntityContainer container;

    /**
     * Child entities.
     */
    protected final DefaultEntityContainer entities;

    /**
     * Associated attributes.
     */
    protected final DefaultAttributeContainer attributes;

    /**
     * Self action scheduler.
     */
    protected final DefaultActionScheduler<Entity> scheduler;

    /**
     * Current state information.
     */
    protected final TagTypeSet tags;

    private final ListenerSet<EntityTypeListener> listeners;
    private final Set<Class<? extends Entity>> types;
    private final AtomicBoolean alive;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a new default entity instance.
     *
     * @param id
     *            Entity ID.
     * @param factory
     *            Entity factory for creating/killing child entities.
     * @param container
     *            Parent entity container.
     */
    protected DefaultEntity(final UUID id, final EntityFactory factory, final EntityContainer container) {
	super(id);
	this.container = container;
	entities = new DefaultEntityContainer(factory, this);
	attributes = new DefaultAttributeContainer(this);
	tags = new TagTypeSet();
	scheduler = new DefaultActionScheduler<>(this);
	listeners = new ListenerSet<>(EntityTypeListener.class);
	types = new HashSet<>();
	alive = new AtomicBoolean();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return attributes.addAttributeListener(name, type, listener);
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addEntityListener(listener);
    }

    @Override
    public boolean addEntityTypeListener(final EntityTypeListener listener) {
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    return listeners.add(listener);
	} finally {
	    write.unlock();
	}
    }

    private void addParentTag() {
	if (container instanceof Identifiable) {
	    tags.add(new Parent(Identifiable.getID(container)));
	}
    }

    @Override
    public void cancelAllScheduledForActor() {
	scheduler.cancelAllScheduledForActor();
    }

    private void checkAlive() {
	if (!isAlive()) {
	    throw new IllegalStateException(String.format("Entity %s is no longer alive", id));
	}
    }

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	attributes.fireAttributeChanged(name, type);
    }

    @Override
    public <T> T getAttribute(final String name, final AttributeType<T> type) {
	return attributes.getAttribute(name, type);
    }

    @Override
    public int getAttributeCount() {
	return attributes.getAttributeCount();
    }

    @Override
    public Set<String> getAttributeListenerNames() {
	return attributes.getAttributeListenerNames();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final String name, final AttributeType<T> type) {
	return attributes.getAttributeListeners(name, type);
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes(final String name) {
	return attributes.getAttributeListenerTypes(name);
    }

    @Override
    public Set<String> getAttributeNames() {
	return attributes.getAttributeNames();
    }

    @Override
    public Set<?> getAttributes() {
	return attributes.getAttributes();
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes(final String name) {
	return attributes.getAttributeTypes(name);
    }

    @Override
    public EntityContainer getContainer() {
	return isAlive() ? container : null;
    }

    /**
     * Gets the associated action engine.
     *
     * @return Optional containing the engine or else empty optional if there is no engine
     *         associated.
     */
    protected ActionEngine getEngine() {
	return scheduler.getEngine();
    }

    @Override
    public Set<Entity> getEntities() {
	return entities.getEntities();
    }

    @Override
    public Entity getEntity(final UUID id) {
	return entities.getEntity(id);
    }

    @Override
    public int getEntityCount() {
	return entities.getEntityCount();
    }

    @Override
    public Set<UUID> getEntityIDs() {
	return entities.getEntityIDs();
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	return entities.getEntityListeners();
    }

    @Override
    public Set<? extends EntityTypeListener> getEntityTypeListeners() {
	read.lock();
	try {
	    return new HashSet<>(listeners);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<Class<? extends Entity>> getMarkedAsTypes() {
	read.lock();
	try {
	    return new HashSet<>(types);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<Tag> getTags() {
	return Collections.unmodifiableSet(tags);
    }

    @Override
    public boolean isAlive() {
	return alive.get();
    }

    @Override
    public boolean isMarkedAsType(final Class<? extends Entity> type) {
	read.lock();
	try {
	    return types.stream().anyMatch(t -> isOrSubtype(t, type));
	} finally {
	    read.unlock();
	}
    }

    @Override
    public boolean kill() {
	return container.killEntity(id);
    }

    @Override
    public void killEntities() {
	entities.killEntities();
    }

    @Override
    public boolean killEntity(final UUID id) {
	return entities.killEntity(id);
    }

    /**
     * Marks the entity as alive.
     *
     * @return Whether the core was alive.
     */
    protected boolean markAsAlive() {
	addParentTag();
	return alive.getAndSet(true);
    }

    /**
     * Marks the entity as dead.
     *
     * @return Whether the core was alive.
     */
    protected boolean markAsDead() {
	tags.removeOfType(Parent.class);
	return alive.getAndSet(false);
    }

    @Override
    public boolean markAsType(final Class<? extends Entity> type) {
	Objects.requireNonNull(type);

	write.lock();
	try {
	    // Add target type
	    if (!types.add(type)) {
		return false;
	    }

	    // Add missing ancestors
	    final Set<Class<? extends Entity>> addedAncestors = new HashSet<>();
	    for (final Class<? extends Entity> at : getTypeAncestry(type)) {
		if (types.add(at)) {
		    // Missing ancestor
		    addedAncestors.add(at);
		}
	    }

	    // Trigger change
	    listeners.getProxy().entityMarkedAsType(new EntityTypeEvent(this, type, addedAncestors));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public MutableActionContext<Entity> newContextForActor(final Action<Entity> action) {
	checkAlive();
	return scheduler.newContextForActor(action);
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer sourceContainer) {
	checkAlive();
	return entities.newEntity(id, sourceContainer);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type, final AttributeContainer sourceContainer) {
	checkAlive();
	return entities.newEntity(id, type, sourceContainer);
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	return entities.receiveEntity(e);
    }

    @Override
    public <T> T removeAttribute(final String name, final AttributeType<T> type) {
	return attributes.removeAttribute(name, type);
    }

    @Override
    public <T> boolean removeAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return attributes.removeAttributeListener(name, type, listener);
    }

    @Override
    public void removeAttributeListeners() {
	attributes.removeAttributeListeners();
    }

    @Override
    public <T> void removeAttributeListeners(final String name, final AttributeType<T> type) {
	attributes.removeAttributeListeners(name, type);
    }

    @Override
    public void removeAttributes() {
	attributes.removeAttributes();
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	return entities.removeEntityListener(listener);
    }

    @Override
    public void removeEntityListeners() {
	entities.removeEntityListeners();
    }

    @Override
    public boolean removeEntityTypeListener(final EntityTypeListener listener) {
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    return listeners.remove(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeEntityTypeListeners() {
	write.lock();
	try {
	    listeners.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public ActionContext<Entity> scheduleForActor(final Action<Entity> action, final long initialDelay,
	    final long period, final TimeUnit unit) {
	checkAlive();
	return scheduler.scheduleForActor(action, initialDelay, period, unit);
    }

    @Override
    public <T> T setAttribute(final String name, final AttributeType<T> type, final T attr) {
	return attributes.setAttribute(name, type, attr);
    }

    /**
     * Sets the parent container for the entity.
     *
     * @param container
     *            New parent container (can be null);
     */
    protected void setContainer(final EntityContainer container) {
	if (!Objects.equals(this.container, container)) {
	    this.container = container;
	    if (container != null && isAlive()) {
		addParentTag();
	    }
	}
    }

    /**
     * Associates an engine to the entity for scheduling actions.
     *
     * @param engine
     *            Engine to set.
     */
    protected void setEngine(final ActionEngine engine) {
	scheduler.setEngine(engine);
    }

    @Override
    public boolean transfer(final EntityContainer destination) {
	return container.transferEntity(id, destination);
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	return entities.transferEntity(id, destination);
    }

    @Override
    public void unmarkAsAllTypes() {
	write.lock();
	try {
	    new HashSet<>(types).forEach(this::unmarkAsType);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean unmarkAsType(final Class<? extends Entity> type) {
	Objects.requireNonNull(type);

	write.lock();
	try {
	    // Remove target type
	    if (!types.remove(type)) {
		return false;
	    }

	    // Remove descendants
	    final Set<Class<? extends Entity>> removedDescendants = new HashSet<>();
	    for (final Class<? extends Entity> dt : types) {
		if (isSubtype(dt, type) && tags.remove(dt)) {
		    // Removed descendant
		    removedDescendants.add(dt);
		}
	    }

	    // Trigger change
	    listeners.getProxy().entityUnmarkedAsType(new EntityTypeEvent(this, type, removedDescendants));

	    return true;
	} finally {
	    write.unlock();
	}
    }
}
