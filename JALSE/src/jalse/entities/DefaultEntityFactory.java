package jalse.entities;

import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_LIMIT_REACHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.ActionEngine;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A {@link EntityFactory} implementation that creates/kills {@link DefaultEntity}. Default entity
 * factory can have a total entity limit set. When this factory kills an entity it will kill the
 * entity tree under it (can only kill entities his factory has created).
 *
 * @author Elliot Ford
 *
 */
public class DefaultEntityFactory implements EntityFactory {

    private final int entityLimit;
    private final Set<UUID> entityIDs;
    private ActionEngine engine;
    private volatile int entityCount;

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
	engine = null;
	entityCount = 0;
    }

    /**
     * Gets the associated engine.
     *
     * @return Action engine.
     */
    public synchronized ActionEngine getEngine() {
	return engine;
    }

    /**
     * Gets the current total entity count.
     *
     * @return Entity count.
     */
    public int getEntityCount() {
	return entityCount;
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

	entityCount--;

	de.killEntities();

	return true;
    }

    @Override
    public synchronized DefaultEntity newEntity(final UUID id, final EntityContainer container) {
	if (entityCount >= entityLimit) {
	    throwRE(ENTITY_LIMIT_REACHED);
	}

	if (!entityIDs.add(id)) {
	    throwRE(ENTITY_ALREADY_ASSOCIATED);
	}

	final DefaultEntity e = new DefaultEntity(id, this, container);
	e.setEngine(engine);
	e.markAsAlive();

	entityCount++;

	return e;
    }

    @Override
    public synchronized void setEngine(final ActionEngine engine) {
	this.engine = Objects.requireNonNull(engine);
    }
}