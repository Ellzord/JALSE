package jalse.entities;

import static jalse.actions.Actions.requireNotStopped;
import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_LIMIT_REACHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.ActionEngine;
import jalse.actions.Actions;
import jalse.actions.ForkJoinActionEngine;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link EntityFactory} implementation that creates/kills {@link DefaultEntity}. Default entity
 * factory can have a total entity limit set. When this factory kills an entity it will kill the
 * entity tree under it (can only kill entities his factory has created).<br>
 * <br>
 * This factory assumes all source containers (and when importing target containers) are genuine. <br>
 * <br>
 * If no {@link ActionEngine} is supplied {@link ForkJoinActionEngine#commonPoolEngine()} will be
 * used.
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityFactory implements EntityFactory {

    private final int entityLimit;
    private final Set<UUID> entityIDs;
    private ActionEngine engine;
    private int entityCount;
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
	entityCount = 0;
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public void exportEntity(final Entity e) {
	write.lock();
	try {
	    if (!entityIDs.remove(e.getID())) {
		throw new IllegalArgumentException("Does not know if this entity");
	    }
	} finally {
	    write.unlock();
	}

	final ActionEngine emptyEngine = Actions.emptyActionEngine();

	final DefaultEntity de = (DefaultEntity) e;
	de.cancelAllScheduledForActor();
	de.setEngine(emptyEngine);
	de.setContainer(null); // Remove parent reference.

	Entities.walkEntities(e).map(DefaultEntity.class::cast).forEach(ce -> {
	    ce.cancelAllScheduledForActor();
	    ce.setEngine(emptyEngine);
	});
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
	    return entityCount;
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
	    if (entityCount >= entityLimit) {
		throwRE(ENTITY_LIMIT_REACHED);
	    }

	    if (!entityIDs.add(id)) { // Unique only
		throwRE(ENTITY_ALREADY_ASSOCIATED);
	    }

	    final DefaultEntity e = new DefaultEntity(id, this, target);
	    e.setEngine(engine);
	    e.markAsAlive();

	    entityCount++;

	    return e;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void setEngine(final ActionEngine engine) {
	write.lock();
	try {
	    this.engine = requireNotStopped(engine);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean tryImportEntity(final Entity e, final EntityContainer target) {
	write.lock();
	try {
	    if (!entityIDs.add(e.getID())) {
		return false;
	    }

	    final DefaultEntity de = (DefaultEntity) e;

	    de.setEngine(engine);
	    de.setContainer(target);

	    Entities.walkEntities(de).map(DefaultEntity.class::cast).forEach(ve -> ve.setEngine(engine));

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean tryKillEntity(final Entity e) {
	write.lock();
	try {
	    final DefaultEntity de = (DefaultEntity) e;

	    if (!entityIDs.remove(de.getID()) || !de.isAlive()) { // Kill only those in need
		return false;
	    }

	    de.markAsDead();
	    de.cancelAllScheduledForActor();
	    de.setEngine(null);

	    entityCount--;

	    de.killEntities(); // Kill tree

	    EntityProxies.removeProxiesOfEntity(de); // Force clean-up

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean tryTakeFromTree(final Entity e, final EntityContainer target) {
	write.lock();
	try {
	    if (!entityIDs.contains(e.getID())) {
		return false;
	    }

	    ((DefaultEntity) e).setContainer(target);

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