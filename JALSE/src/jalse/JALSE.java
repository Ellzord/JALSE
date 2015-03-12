package jalse;

import jalse.actions.Action;
import jalse.actions.ActionEngine;
import jalse.actions.ActionScheduler;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.EntityFactory;
import jalse.entities.EntitySet;
import jalse.listeners.EngineListener;
import jalse.listeners.EntityListener;
import jalse.misc.Engine;
import jalse.tags.Tag;
import jalse.tags.TagSet;
import jalse.tags.Taggable;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JALSE is the overall container and engine for each simulation. It provides the ability to create
 * a number of {@link Entity} and execute {@link Action} at given intervals. Although {@link Entity}
 * can be created/killed no {@link Action} will run until {@link Engine#tick()} is called.
 *
 * @author Elliot Ford
 *
 */
public class JALSE implements Engine, EntityContainer, Taggable, ActionScheduler<JALSE> {

    private volatile int totalEntityCount;

    /**
     * Backing entity set for top level entities.
     */
    protected final EntitySet entities;

    /**
     * Current state information.
     */
    protected final TagSet tags;

    /**
     * Action engine to be supplied to entities.
     */
    protected final ActionEngine engine;

    /**
     * Entity factory for creating/killing entities.
     */
    protected final EntityFactory factory;

    /**
     * Creates a new instance of JALSE with the supplied engine and factory.
     * 
     * @param engine
     *            Action engine to associate to factory and schedule actions.
     * @param factory
     *            Entity factory to create/kill child entities.
     *
     */
    public JALSE(final ActionEngine engine, final EntityFactory factory) {
	this.engine = Objects.requireNonNull(engine);
	this.factory = Objects.requireNonNull(factory);
	factory.setEngine(engine);
	entities = new EntitySet(factory, this);
	tags = new TagSet();
    }

    @Override
    public boolean addEngineListener(final EngineListener listener) {
	return engine.addEngineListener(listener);
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addListener(listener);
    }

    @Override
    public boolean cancel(final UUID action) {
	return engine.cancel(action);
    }

    @Override
    public Set<? extends EngineListener> getEngineListeners() {
	return engine.getEngineListeners();
    }

    @Override
    public Set<Entity> getEntities() {
	return streamEntities().collect(Collectors.toSet());
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {
	return entities.getOfType(type);
    }

    @Override
    public Optional<Entity> getEntity(final UUID id) {
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
    public int getState() {
	return engine.getState();
    }

    @Override
    public Set<Tag> getTags() {
	return Collections.unmodifiableSet(tags);
    }

    @Override
    public TickInfo getTickInfo() {
	return engine.getTickInfo();
    }

    /**
     * Gets the current total number of entities.
     *
     * @return Total number of entities within the simulation.
     */
    public int getTotalEntityCount() {
	return totalEntityCount;
    }

    @Override
    public boolean isActive(final UUID action) {
	return engine.isActive(action);
    }

    @Override
    public void killEntities() {
	entities.clear();
    }

    @Override
    public boolean killEntity(final UUID id) {
	return entities.killEntity(id);
    }

    @Override
    public Entity newEntity() {
	return entities.newEntity();
    }

    @Override
    public <T extends Entity> T newEntity(final Class<T> type) {
	return entities.newEntity(type);
    }

    @Override
    public Entity newEntity(final UUID id) {
	return entities.newEntity(id);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	return entities.newEntity(id, type);
    }

    @Override
    public void pause() {
	engine.pause();
    }

    @Override
    public boolean removeEngineListener(final EngineListener listener) {
	return engine.removeEngineListener(listener);
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	return entities.removeListener(listener);
    }

    @Override
    public UUID scheduleAction(final Action<JALSE> action, final long initialDelay, final long period,
	    final TimeUnit unit) {
	return engine.scheduleAction(action, this, initialDelay, period, unit);
    }

    /**
     * Sets the first action of a tick to be run.
     *
     * @param action
     *            Action to set.
     *
     */
    public void setFirstAction(final Action<JALSE> action) {
	engine.setFirstAction(action, this);
    }

    /**
     * Sets the last action of a tick to be run.
     *
     * @param action
     *            Action to set.
     *
     */
    public void setLastAction(final Action<JALSE> action) {
	engine.setLastAction(action, this);
    }

    @Override
    public void stop() {
	engine.stop();
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
    public void tick() {
	engine.tick();
    }
}
