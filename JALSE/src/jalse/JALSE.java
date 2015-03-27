package jalse;

import static jalse.actions.Actions.requireNotStopped;
import jalse.actions.Action;
import jalse.actions.ActionEngine;
import jalse.actions.ActionScheduler;
import jalse.actions.DefaultActionScheduler;
import jalse.actions.MutableActionBindings;
import jalse.actions.MutableActionContext;
import jalse.entities.Entities;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.EntityFactory;
import jalse.entities.EntitySet;
import jalse.listeners.EntityListener;
import jalse.misc.AbstractIdentifiable;
import jalse.tags.Tag;
import jalse.tags.TagSet;
import jalse.tags.Taggable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JALSE is the overall parent container and engine for each simulation. It provides the ability to
 * create a number of {@link Entity} and execute {@link Action} at given intervals.
 *
 *
 * @author Elliot Ford
 *
 * @see ActionEngine
 * @see EntitySet
 * @see TagSet
 * @see EntityFactory
 *
 */
public class JALSE extends AbstractIdentifiable implements ActionEngine, ActionScheduler<JALSE>, EntityContainer,
	Taggable {

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
     * Self action scheduler.
     */
    protected final DefaultActionScheduler<JALSE> scheduler;

    /**
     * Entity factory for creating/killing entities.
     */
    protected final EntityFactory factory;

    /**
     * Creates a new instance of JALSE with the supplied engine and factory.
     *
     * @param id
     *            The ID used to identify between JALSE instances.
     *
     * @param engine
     *            Action engine to associate to factory and schedule actions.
     * @param factory
     *            Entity factory to create/kill child entities.
     *
     */
    public JALSE(final UUID id, final ActionEngine engine, final EntityFactory factory) {
	super(id);
	this.engine = requireNotStopped(engine);
	this.factory = Objects.requireNonNull(factory);
	factory.setEngine(engine);
	scheduler = new DefaultActionScheduler<>(this);
	scheduler.setEngine(engine);
	entities = new EntitySet(factory, this);
	tags = new TagSet();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addListener(listener);
    }

    @Override
    public void cancelAllScheduledForActor() {
	scheduler.cancelAllScheduledForActor();
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return engine.createContext(action);
    }

    /**
     * Gets the current set of IDs for the entire tree.
     *
     * @return All entity IDs.
     */
    public Set<UUID> getAllEntityIDs() {
	return Entities.getEntityIDsRecursively(this);
    }

    @Override
    public MutableActionBindings getBindings() {
	return engine.getBindings();
    }

    @Override
    public Set<Entity> getEntities() {
	return streamEntities().collect(Collectors.toSet());
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
    public Entity getOrNullEntity(final UUID id) {
	return entities.getEntity(id);
    }

    @Override
    public Set<Tag> getTags() {
	return Collections.unmodifiableSet(tags);
    }

    /**
     * Gets the current total number of entities.
     *
     * @return Total number of entities within the simulation.
     */
    public int getTotalEntityCount() {
	return Entities.getEntityCountRecursively(this);
    }

    @Override
    public boolean isPaused() {
	return engine.isPaused();
    }

    @Override
    public boolean isStopped() {
	return engine.isStopped();
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
    public boolean receiveEntity(final Entity e) {
	if (Entities.withinSameTree(e, this)) {
	    return entities.receiveFromTree(e); // Wont import.
	}
	return entities.receive(e);
    }

    @Override
    public void removeAllEntityListeners() {
	entities.removeAllListeners();
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	return entities.removeListener(listener);
    }

    @Override
    public void resume() {
	engine.resume();
    }

    @Override
    public MutableActionContext<JALSE> scheduleForActor(final Action<JALSE> action, final long initialDelay,
	    final long period, final TimeUnit unit) {
	return scheduler.scheduleForActor(action, initialDelay, period, unit);
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
    public <T extends Entity> Stream<T> streamEntitiesAsType(final Class<T> type) {
	return entities.streamAsType(type);
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return entities.streamOfType(type);
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	return entities.transferOrExport(id, destination);
    }
}
