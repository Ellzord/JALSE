package jalse.entities;

import static jalse.entities.Entities.isOrSubtype;
import static jalse.misc.JALSEExceptions.ENTITY_NOT_ALIVE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.ActionEngine;
import jalse.actions.DefaultActionScheduler;
import jalse.actions.MutableActionContext;
import jalse.attributes.AttributeContainer;
import jalse.attributes.AttributeType;
import jalse.attributes.DefaultAttributeContainer;
import jalse.listeners.AttributeListener;
import jalse.listeners.EntityContainerListener;
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
	entities = new DefaultEntityContainer(factory, this);
	attributes = new DefaultAttributeContainer(this);
	tags = new TagTypeSet();
	scheduler = new DefaultActionScheduler<>(this);
	alive = new AtomicBoolean();
    }

    @Override
    public <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return attributes.addAttributeListener(name, type, listener);
    }

    @Override
    public boolean addEntityContainerListener(final EntityContainerListener listener) {
	return entities.addEntityContainerListener(listener);
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
    public Entity getEntity(final UUID id) {
	return entities.getEntity(id);
    }

    @Override
    public Set<? extends EntityContainerListener> getEntityContainerListeners() {
	return entities.getEntityContainerListeners();
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
    public Set<Tag> getTags() {
	return Collections.unmodifiableSet(tags);
    }

    @Override
    public boolean isAlive() {
	return alive.get();
    }

    @Override
    public boolean isMarkedAsType(final Class<? extends Entity> type) {
	return tags.getOfType(EntityType.class).stream().anyMatch(at -> isOrSubtype(at.getType(), type));
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
    public MutableActionContext<Entity> newContextForActor(final Action<Entity> action) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}
	return scheduler.newContextForActor(action);
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer sourceContainer) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

	return entities.newEntity(id, sourceContainer);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type, final AttributeContainer sourceContainer) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}

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
    public boolean removeEntityContainerListener(final EntityContainerListener listener) {
	return entities.removeEntityContainerListener(listener);
    }

    @Override
    public void removeEntityContainerListeners() {
	entities.removeEntityContainerListeners();
    }

    @Override
    public ActionContext<Entity> scheduleForActor(final Action<Entity> action, final long initialDelay,
	    final long period, final TimeUnit unit) {
	if (!isAlive()) {
	    throwRE(ENTITY_NOT_ALIVE);
	}
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
    public Stream<?> streamAttributes() {
	return attributes.streamAttributes();
    }

    @Override
    public Stream<Entity> streamEntities() {
	return entities.streamEntities();
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
    public boolean unmarkAsType(final Class<? extends Entity> type) {
	final Set<EntityType> descendants = tags.getOfType(EntityType.class).stream()
		.filter(at -> isOrSubtype(at.getType(), type)).collect(Collectors.toSet());

	/*
	 * Remove subclasses of the type (up tree)
	 */
	descendants.forEach(tags::remove);

	return !descendants.isEmpty();
    }
}
