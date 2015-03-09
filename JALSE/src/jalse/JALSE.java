package jalse;

import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_LIMIT_REACHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.AbstractEngine;
import jalse.actions.Action;
import jalse.actions.Scheduler;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.EntityFactory;
import jalse.entities.EntitySet;
import jalse.listeners.EntityListener;
import jalse.tags.Tag;
import jalse.tags.TagSet;
import jalse.tags.Taggable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JALSE is the overall container and engine for each simulation. It provides the ability to create
 * a number of {@link Entity} and execute {@link Action} at given intervals. Although {@link Entity}
 * can be created/killed no {@link Action} will run until {@link AbstractEngine#tick()} is called.
 *
 * @author Elliot Ford
 *
 */
public class JALSE extends AbstractEngine implements EntityContainer, Taggable, Scheduler<JALSE> {

    private class LimitingEntityFactory implements EntityFactory {

	private final Set<UUID> entityIDs;

	private LimitingEntityFactory() {
	    entityIDs = new HashSet<>();
	}

	@Override
	public synchronized boolean killEntity(final Entity e) {
	    if (!(e instanceof DefaultEntity)) {
		return false;
	    }

	    final DefaultEntity de = (DefaultEntity) e;

	    if (!de.isAlive() || !entityIDs.remove(de.getID())) {
		return false;
	    }

	    de.markAsDead();
	    de.cancelTasks();
	    de.setEngine(null);

	    totalEntityCount--;

	    de.killEntities();

	    return true;
	}

	@Override
	public synchronized Entity newEntity(final UUID id, final EntityContainer container) {
	    if (totalEntityCount >= totalEntityLimit) {
		throwRE(ENTITY_LIMIT_REACHED);
	    }

	    if (!entityIDs.add(id)) {
		throwRE(ENTITY_ALREADY_ASSOCIATED);
	    }

	    final DefaultEntity e = new DefaultEntity(id, LimitingEntityFactory.this, container);
	    e.setEngine(JALSE.this);
	    e.markAsAlive();

	    totalEntityCount++;

	    return e;
	}
    }

    private volatile int totalEntityCount;
    private final int totalEntityLimit;

    /**
     * Backing entity set for top level entities.
     */
    protected final EntitySet entities;

    /**
     * Current state information.
     */
    protected final TagSet tags;

    /**
     * Creates a new instance of JALSE with the given qualities.
     *
     * @param tps
     *            Number of ticks per second the engine should tick at.
     * @param totalThreads
     *            Total number of threads the engine should use.
     * @param totalEntityLimit
     *            Maximum number of entities.
     * @throws IllegalArgumentException
     *             All parameters must be above zero.
     */
    public JALSE(final int tps, final int totalThreads, final int totalEntityLimit) {
	super(tps, totalThreads);

	if (totalEntityLimit <= 0) {
	    throw new IllegalArgumentException();
	}

	this.totalEntityLimit = totalEntityLimit;
	entities = new EntitySet(new LimitingEntityFactory(), this);
	tags = new TagSet();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addListener(listener);
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

    /**
     * Gets the first action run each tick.
     *
     * @return First action to be run or null if none set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Action<JALSE> getFirstAction() {
	return (Action<JALSE>) super.getFirstAction();
    }

    /**
     * Gets the last action run each tick.
     *
     * @return Last action to be run or null if none set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Action<JALSE> getLastAction() {
	return (Action<JALSE>) super.getLastAction();
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
	return totalEntityCount;
    }

    /**
     * Gets the total entity limit.
     *
     * @return Total entity limit JALSE was initialised with.
     */
    public int getTotalEntityLimit() {
	return totalEntityLimit;
    }

    /**
     * Gets whether the first action of tick a has been set.
     *
     * @return Whether first action has been set.
     *
     * @see #getFirstAction()
     * @see #setFirstAction(Action)
     */
    public boolean hasFirstAction() {
	return super.getFirstAction() != null;
    }

    /**
     * Gets whether the last action of a tick has been set.
     *
     * @return Whether last action has been set.
     *
     * @see #getLastAction()
     * @see #setLastAction(Action)
     */
    public boolean hasLastAction() {
	return super.getLastAction() != null;
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
    public boolean removeEntityListener(final EntityListener listener) {
	return entities.removeListener(listener);
    }

    @Override
    public UUID scheduleAction(final Action<JALSE> action, final long initialDelay, final long period,
	    final TimeUnit unit) {
	return scheduleAction(action, this, initialDelay, period, unit);
    }

    /**
     * Sets the first action of a tick to be run.
     *
     * @param action
     *            Action to set.
     *
     */
    public void setFirstAction(final Action<JALSE> action) {
	setFirstAction(action, this);
    }

    /**
     * Sets the last action of a tick to be run.
     *
     * @param action
     *            Action to set.
     *
     */
    public void setLastAction(final Action<JALSE> action) {
	setLastAction(action, this);
    }

    @Override
    public Stream<Entity> streamEntities() {
	return entities.stream();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return entities.streamOfType(type);
    }

}
