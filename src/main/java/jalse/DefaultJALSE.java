package jalse;

import static jalse.actions.Actions.requireNotStopped;
import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.ActionEngine;
import jalse.actions.DefaultActionScheduler;
import jalse.actions.ForkJoinActionEngine;
import jalse.actions.MutableActionBindings;
import jalse.actions.MutableActionContext;
import jalse.attributes.AttributeContainer;
import jalse.entities.DefaultEntityContainer;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entities;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.entities.EntityFactory;
import jalse.listeners.EntityListener;
import jalse.misc.AbstractIdentifiable;
import jalse.tags.Tag;
import jalse.tags.TagTypeSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A {@link JALSE} implementation that using default implementations.<br>
 * <br>
 * DefaultJALSE uses {@link DefaultEntityFactory} (with no {@link Entity} limit) and
 * {@link ForkJoinActionEngine#commonPoolEngine()} by default if no {@link EntityFactory} or
 * {@link ActionEngine} are provided.<br>
 *
 * @author Elliot Ford
 *
 */
public class DefaultJALSE extends AbstractIdentifiable implements JALSE {

    /**
     * Backing entity container for top level entities.
     */
    protected final DefaultEntityContainer entities;

    /**
     * Current state information.
     */
    protected final TagTypeSet tags;

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
     * Creates a new instance of DefaultJALSE using the common pool engine and default entity
     * factory (no limit).
     *
     * @param id
     *            The ID used to identify between JALSE instances.
     */
    public DefaultJALSE(final UUID id) {
	this(id, ForkJoinActionEngine.commonPoolEngine());
    }

    /**
     * Creates a new instance of DefaultJALSE using the default entity factory (no limit).
     *
     * @param id
     *            The ID used to identify between JALSE instances.
     * @param engine
     *            Action engine to associate to factory and schedule actions.
     */
    public DefaultJALSE(final UUID id, final ActionEngine engine) {
	this(id, engine, new DefaultEntityFactory());
    }

    /**
     * Creates a new instance of DefaultJALSE with the supplied engine and factory.
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
    public DefaultJALSE(final UUID id, final ActionEngine engine, final EntityFactory factory) {
	super(id);
	this.engine = requireNotStopped(engine);
	this.factory = Objects.requireNonNull(factory);
	factory.setEngine(engine);
	scheduler = new DefaultActionScheduler<>(this);
	scheduler.setEngine(engine);
	entities = new DefaultEntityContainer(factory, this);
	tags = new TagTypeSet();
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return entities.addEntityListener(listener);
    }

    @Override
    public void cancelAllScheduledForActor() {
	scheduler.cancelAllScheduledForActor();
    }

    @Override
    public MutableActionBindings getBindings() {
	return engine.getBindings();
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
    public Set<UUID> getIDsInTree() {
	return Entities.getEntityIDsRecursively(entities);
    }

    @Override
    public Set<Tag> getTags() {
	return Collections.unmodifiableSet(tags);
    }

    @Override
    public int getTreeCount() {
	return Entities.getEntityCountRecursively(entities);
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
	entities.killEntities();
    }

    @Override
    public boolean killEntity(final UUID id) {
	return entities.killEntity(id);
    }

    @Override
    public <T> MutableActionContext<T> newContext(final Action<T> action) {
	return engine.newContext(action);
    }

    @Override
    public MutableActionContext<JALSE> newContextForActor(final Action<JALSE> action) {
	return scheduler.newContextForActor(action);
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer sourceContainer) {
	return entities.newEntity(id, sourceContainer);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type, final AttributeContainer sourceContainer) {
	return entities.newEntity(id, type, sourceContainer);
    }

    @Override
    public void pause() {
	engine.pause();
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	return entities.receiveEntity(e);
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
    public void resume() {
	engine.resume();
    }

    @Override
    public ActionContext<JALSE> scheduleForActor(final Action<JALSE> action, final long initialDelay,
	    final long period, final TimeUnit unit) {
	return scheduler.scheduleForActor(action, initialDelay, period, unit);
    }

    @Override
    public void stop() {
	engine.stop();
    }

    @Override
    public Stream<Entity> streamEntities() {
	return entities.streamEntities();
    }

    @Override
    public Stream<Entity> streamEntityTree() {
	return Entities.walkEntities(entities);
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	return entities.transferEntity(id, destination);
    }
}
