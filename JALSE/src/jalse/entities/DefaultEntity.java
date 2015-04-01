package jalse.entities;

import static jalse.entities.Entities.isOrTypeDescendant;
import static jalse.misc.JALSEExceptions.ENTITY_NOT_ALIVE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.actions.ActionEngine;
import jalse.actions.DefaultActionScheduler;
import jalse.actions.MutableActionContext;
import jalse.attributes.AttributeSet;
import jalse.attributes.AttributeType;
import jalse.listeners.AttributeListener;
import jalse.listeners.EntityListener;
import jalse.misc.AbstractIdentifiable;
import jalse.misc.Identifiable;
import jalse.tags.EntityType;
import jalse.tags.Parent;
import jalse.tags.Tag;
import jalse.tags.TagTypeSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple yet fully featured {@link Entity} implementation.<br>
 * <br>
 * This entity can be marked as alive ({@link #markAsAlive()}) or dead ({@link #markAsDead()}).
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityFactory
 * @see EntitySet
 * @see AttributeSet
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
    protected final EntitySet entities;

    /**
     * Associated attributes.
     */
    protected final AttributeSet attributes;

    /**
     * Self action scheduler.
     */
    protected final DefaultActionScheduler<Entity> scheduler;

    /**
     * Current state information.
     */
    protected final TagTypeSet tags;

    private final AtomicBoolean alive;

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
	entities = new EntitySet(factory, this);
	attributes = new AttributeSet(this);
	tags = new TagTypeSet();
	scheduler = new DefaultActionScheduler<>(this);
	alive = new AtomicBoolean();
    }

    @Override
    public <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return attributes.addListener(name, type, listener);
    }

    @Override
    public <T> T addAttributeOfType(final String name, final AttributeType<T> type, final T attr) {
	return attributes.addOfType(name, type, attr);
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addListener(listener);
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

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	attributes.fireChanged(name, type);
    }

    @Override
    public int getAttributeCount() {
	return attributes.size();
    }

    @Override
    public Set<String> getAttributeListenerNames() {
	return attributes.getListenerNames();
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final String name, final AttributeType<T> type) {
	return attributes.getListeners(name, type);
    }

    @Override
    public Set<AttributeType<?>> getAttributeListenerTypes(final String name) {
	return attributes.getListenerTypes(name);
    }

    @Override
    public Set<String> getAttributeNames() {
	return attributes.getNames();
    }

    @Override
    public <T> T getAttributeOfType(final String name, final AttributeType<T> type) {
	return attributes.getOfType(name, type);
    }

    @Override
    public Set<?> getAttributes() {
	return Collections.unmodifiableSet(attributes);
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes(final String name) {
	return attributes.getTypes(name);
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
	return Collections.unmodifiableSet(entities);
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesAsType(final Class<T> type) {
	return entities.getAsType(type);
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {
	return entities.getOfType(type);
    }

    @Override
    public Entity getEntity(final UUID id) {
	return entities.getEntity(id);
    }

    @Override
    public int getEntityCount() {
	return entities.size();
    }

    @Override
    public Set<UUID> getEntityIDs() {
	return entities.getEntityIDs();
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	return entities.getListeners();
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
	return tags.getOfType(EntityType.class).stream().anyMatch(at -> isOrTypeDescendant(at.getType(), type));
    }

    @Override
    public boolean kill() {
	return container.killEntity(id);
    }

    @Override
    public void killEntities() {
	entities.clear();
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
	if (!isMarkedAsType(type)) {
	    tags.add(new EntityType(type));

	    /*
	     * Add entire tree
	     */
	    Entities.getTypeAncestry(type).stream().map(EntityType::new).forEach(tags::add);
	    return true;
	}

	return false;
    }

    @Override
    public Entity newEntity() {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return entities.newEntity();
    }

    @Override
    public <T extends Entity> T newEntity(final Class<T> type) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return entities.newEntity(type);
    }

    @Override
    public Entity newEntity(final UUID id) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return entities.newEntity(id);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return entities.newEntity(id, type);
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	return entities.receive(e);
    }

    /**
     * Receives an entity from within the tree (so does not need to import).
     *
     * @param e
     *            Entity to receive.
     * @return Whether the entity was added.
     */
    protected boolean receiveFromTree(final Entity e) {
	return entities.receiveFromTree(e);
    }

    @Override
    public void removeAllEntityListeners() {
	entities.removeAllListeners();
    }

    @Override
    public <T> boolean removeAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return attributes.removeListener(name, type, listener);
    }

    @Override
    public <T> void removeAttributeListeners(final String name, final AttributeType<T> type) {
	attributes.removeListeners(name, type);
    }

    @Override
    public <T> T removeAttributeOfType(final String name, final AttributeType<T> type) {
	return attributes.removeOfType(name, type);
    }

    @Override
    public void removeAttributes() {
	attributes.clear();
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	return entities.removeListener(listener);
    }

    @Override
    public MutableActionContext<Entity> scheduleForActor(final Action<Entity> action, final long initialDelay,
	    final long period, final TimeUnit unit) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return scheduler.scheduleForActor(action, initialDelay, period, unit);
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
    public Stream<?> streamAttributes() {
	return attributes.stream();
    }

    @Override
    public Stream<Entity> streamEntities() {
	return entities.stream();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesAsType(final Class<T> type) {
	return entities.streamAsType(type);
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return entities.streamOfType(type);
    }

    @Override
    public boolean transfer(final EntityContainer destination) {
	return container.transferEntity(id, destination);
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	return entities.transferOrExport(id, destination);
    }

    @Override
    public boolean unmarkAsType(final Class<? extends Entity> type) {
	final Set<EntityType> descendants = tags.getOfType(EntityType.class).stream()
		.filter(at -> isOrTypeDescendant(at.getType(), type)).collect(Collectors.toSet());

	/*
	 * Remove subclasses of the type (up tree)
	 */
	descendants.forEach(tags::remove);

	return !descendants.isEmpty();
    }
}
