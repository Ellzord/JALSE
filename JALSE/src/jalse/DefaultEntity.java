package jalse;

import static jalse.misc.JALSEExceptions.ENTITY_NOT_ALIVE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.AbstractEngine;
import jalse.actions.Action;
import jalse.actions.DefaultScheduler;
import jalse.attributes.Attribute;
import jalse.attributes.AttributeSet;
import jalse.entities.Entities;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.EntityFactory;
import jalse.entities.EntitySet;
import jalse.listeners.AttributeListener;
import jalse.listeners.EntityListener;
import jalse.misc.AbstractIdentifiable;
import jalse.misc.Identifiable;
import jalse.tags.EntityType;
import jalse.tags.Parent;
import jalse.tags.Tag;
import jalse.tags.TagSet;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultEntity extends AbstractIdentifiable implements Entity {

    protected final EntityContainer container;
    protected final EntitySet entities;
    protected final AttributeSet attributes;
    protected final DefaultScheduler<Entity> scheduler;
    protected final TagSet tags;
    private final AtomicBoolean alive;

    DefaultEntity(final UUID id, final EntityFactory factory, final EntityContainer container) {

	super(id);

	this.container = container;

	entities = new EntitySet(factory, this);
	attributes = new AttributeSet(this);
	tags = new TagSet();
	scheduler = new DefaultScheduler<>(this);
	alive = new AtomicBoolean();
    }

    @Override
    public boolean addAttributeListener(final AttributeListener<? extends Attribute> listener) {

	return attributes.addListener(listener);
    }

    @Override
    public <S extends Attribute> Optional<S> addAttributeOfType(final S attr) {

	return attributes.addOfType(attr);
    }

    @Override
    public boolean cancel(final UUID action) {

	return scheduler.cancel(action);
    }

    @Override
    public void cancelTasks() {

	scheduler.cancelTasks();
    }

    @Override
    public <S extends Attribute> boolean fireAttributeChanged(final Class<S> attr) {

	return attributes.fireChanged(attr);
    }

    @Override
    public <S extends Attribute> Set<? extends AttributeListener<S>> getAttributeListeners(final Class<S> attr) {

	return attributes.getListeners(attr);
    }

    @Override
    public <S extends Attribute> Optional<S> getAttributeOfType(final Class<S> attr) {

	return attributes.getOfType(attr);
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
    public Set<Entity> getEntities() {

	return Collections.unmodifiableSet(entities);
    }

    protected Optional<AbstractEngine> getEngine() {

	return Optional.ofNullable(scheduler.getEngine());
    }

    @Override
    public Optional<EntityContainer> getContainer() {

	return Optional.ofNullable(isAlive() ? container : null);
    }

    @Override
    public Set<Tag> getTags() {

	return Collections.unmodifiableSet(tags);
    }

    @Override
    public boolean isActive(final UUID action) {

	return scheduler.isActive(action);
    }

    @Override
    public boolean isAlive() {

	return alive.get();
    }

    @Override
    public boolean isMarkedAsType(final Class<? extends Entity> type) {

	return tags.getOfType(EntityType.class).stream().anyMatch(at -> Entities.isOrDescendant(at.getType(), type));
    }

    @Override
    public boolean kill() {

	return container.killEntity(getID());
    }

    @Override
    public boolean killEntity(final UUID id) {

	return entities.killEntity(id);
    }

    protected boolean markAsAlive() {

	if (container instanceof Identifiable) {

	    tags.add(new Parent(Identifiable.getID(container)));
	}

	return alive.getAndSet(true);
    }

    protected boolean markAsDead() {

	tags.removeOfType(Parent.class);

	return alive.getAndSet(false);
    }

    @Override
    public boolean markAsType(final Class<? extends Entity> type) {

	if (!isMarkedAsType(type)) {

	    tags.add(new EntityType(type));
	    Entities.getAncestry(type).stream().map(t -> new EntityType(t)).forEach(tags::add);

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
    public boolean removeAttributeListener(final AttributeListener<? extends Attribute> listener) {

	return attributes.removeListener(listener);
    }

    @Override
    public <S extends Attribute> Optional<S> removeAttributeOfType(final Class<S> attr) {

	return attributes.removeOfType(attr);
    }

    @Override
    public UUID scheduleAction(final Action<Entity> action, final long initialDelay, final long period,
	    final TimeUnit unit) {

	if (!isAlive()) {

	    throwRE(ENTITY_NOT_ALIVE);
	}

	return scheduler.scheduleAction(action, initialDelay, period, unit);
    }

    protected Optional<AbstractEngine> setEngine(final AbstractEngine engine) {

	final AbstractEngine previous = scheduler.getEngine();

	scheduler.setEngine(engine);

	return Optional.ofNullable(previous);
    }

    @Override
    public Stream<Entity> streamEntities() {

	return entities.stream();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {

	return entities.streamOfType(type);
    }

    @Override
    public boolean unmarkAsType(final Class<? extends Entity> type) {

	final Set<EntityType> descendants = tags.getOfType(EntityType.class).stream()
		.filter(at -> Entities.isOrDescendant(at.getType(), type)).collect(Collectors.toSet());

	descendants.forEach(tags::remove);

	return !descendants.isEmpty();
    }

    @Override
    public Optional<Entity> getEntity(final UUID id) {

	return entities.getEntity(id);
    }

    @Override
    public Set<? extends AttributeListener<? extends Attribute>> getAttributeListeners() {

	return attributes.getListeners();
    }

    @Override
    public int getAttributeCount() {

	return attributes.size();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {

	return entities.addListener(listener);
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {

	return entities.removeListener(listener);
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {

	return entities.getListeners();
    }

    @Override
    public Set<? extends Attribute> getAttributes() {

	return Collections.unmodifiableSet(attributes);
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {

	return entities.getOfType(type);
    }

    @Override
    public void killEntities() {

	entities.clear();
    }

    @Override
    public void removeAttributes() {

	attributes.clear();
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeTypes() {

	return attributes.getAttributeTypes();
    }

    @Override
    public Set<Class<? extends Attribute>> getAttributeListenerTypes() {

	return attributes.getAttributeListenerTypes();
    }

    @Override
    public Stream<? extends Attribute> streamAttributes() {

	return attributes.stream();
    }
}
