package jalse.entities;

import static jalse.actions.Actions.requireNotStopped;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import jalse.actions.ActionEngine;
import jalse.actions.Actions;
import jalse.actions.ForkJoinActionEngine;

/**
 * A {@link EntityFactory} implementation that creates/kills {@link DefaultEntity}. Default entity
 * factory can have a total entity limit set. When this factory kills an entity it will kill the
 * entity tree under it (can only kill entities his factory has created).<br>
 * <br>
 * This factory assumes all source containers (and when importing target containers) are genuine.
 * <br>
 * <br>
 * If no {@link ActionEngine} is supplied {@link ForkJoinActionEngine#commonPoolEngine()} will be
 * used.
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityFactory implements EntityFactory {

    private static final Logger logger = Logger.getLogger(DefaultEntityFactory.class.getName());

    private final int entityLimit;
    private final Set<UUID> entityIDs;
    private ActionEngine engine;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a default entity factory with no entity limit.
     */
    public DefaultEntityFactory() {
	this(Integer.MAX_VALUE);
    }

    /**
     * Creates a default entity factory with the supplied entity limit.
     *
     * @param entityLimit
     *            Maximum entity limit.
     */
    public DefaultEntityFactory(final int entityLimit) {
	if (entityLimit <= 0) {
	    throw new IllegalArgumentException();
	}
	this.entityLimit = entityLimit;
	entityIDs = new HashSet<>();
	engine = ForkJoinActionEngine.commonPoolEngine(); // Defaults use common engine
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public void exportEntity(final Entity e) {
	final UUID eID = e.getID();

	write.lock();
	try {
	    if (!entityIDs.remove(eID)) {
		throw new IllegalArgumentException(String.format("Does not know of entity %s", eID));
	    }

	    final ActionEngine emptyEngine = Actions.emptyActionEngine();

	    final DefaultEntity de = (DefaultEntity) e;
	    de.cancelAllScheduledForActor();
	    de.setEngine(emptyEngine);
	    de.setContainer(null); // Remove parent reference.

	    Entities.walkEntities(e).map(DefaultEntity.class::cast).forEach(ce -> {
		entityIDs.remove(ce.getID());
		ce.cancelAllScheduledForActor();
		ce.setEngine(emptyEngine);
	    });

	    logger.fine(String.format("Entity %s exported", eID));
	} finally {
	    write.unlock();
	}
    }

    /**
     * Gets the associated engine.
     *
     * @return Action engine.
     */
    public ActionEngine getEngine() {
	read.lock();
	try {
	    return engine;
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets the current total entity count.
     *
     * @return Entity count.
     */
    public int getEntityCount() {
	read.lock();
	try {
	    return entityIDs.size();
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets the total entity limit.
     *
     * @return Entity limit.
     */
    public int getEntityLimit() {
	return entityLimit;
    }

    @Override
    public DefaultEntity newEntity(final UUID id, final EntityContainer target) {
	Objects.requireNonNull(id);
	Objects.requireNonNull(target);

	write.lock();
	try {
	    if (entityIDs.size() >= entityLimit) {
		throw new IllegalStateException(String.format("Entity limit of %d has been reached", entityLimit));
	    }

	    // Unique only
	    if (!entityIDs.add(id)) {
		throw new IllegalArgumentException(String.format("Entity %s is already associated", id));
	    }

	    final DefaultEntity e = new DefaultEntity(id, this, target);
	    e.setEngine(engine);
	    e.markAsAlive();

	    logger.fine(String.format("Entity %s created", id));

	    return e;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void setEngine(final ActionEngine engine) {
	Objects.requireNonNull(engine);

	write.lock();
	try {
	    logger.fine(String.format("Switching engine type %s to %s", this.engine.getClass(), engine.getClass()));
	    this.engine = requireNotStopped(engine);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public String toString() {
	return "DefaultEntityFactory [entityLimit=" + entityLimit + ", entityCount=" + getEntityCount() + "]";
    }

    @Override
    public boolean tryImportEntity(final Entity e, final EntityContainer target) {
	final UUID eID = e.getID();

	write.lock();
	try {
	    if (!entityIDs.add(eID)) {
		return false;
	    }

	    final DefaultEntity de = (DefaultEntity) e;
	    de.setEngine(engine);
	    de.setContainer(target);

	    Entities.walkEntities(de).map(DefaultEntity.class::cast).forEach(ve -> {
		entityIDs.add(eID);
		ve.setEngine(engine);
	    });

	    logger.fine(String.format("Entity %s imported", eID));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean tryKillEntity(final Entity e) {
	final UUID eID = e.getID();

	write.lock();
	try {
	    final DefaultEntity de = (DefaultEntity) e;

	    if (!entityIDs.remove(eID) || !de.isAlive()) { // Kill only those in need
		return false;
	    }

	    de.markAsDead();
	    de.cancelAllScheduledForActor();
	    de.setEngine(null);

	    de.killEntities(); // Kill tree

	    logger.fine(String.format("Entity %s killed", eID));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean tryTakeFromTree(final Entity e, final EntityContainer target) {
	final UUID eID = e.getID();

	write.lock();
	try {
	    if (!entityIDs.contains(eID)) {
		return false;
	    }

	    ((DefaultEntity) e).setContainer(target);

	    logger.fine(String.format("Entity %s taken from tree", eID));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean withinSameTree(final EntityContainer source, final EntityContainer target) {
	Objects.requireNonNull(source);
	Objects.requireNonNull(target);

	read.lock();
	try {
	    return Entities.withinSameTree(source, target);
	} finally {
	    read.unlock();
	}
    }
}