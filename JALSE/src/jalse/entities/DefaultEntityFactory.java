package jalse.entities;

import static jalse.actions.Actions.requireNotStopped;
import static jalse.misc.JALSEExceptions.ENTITY_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.ENTITY_LIMIT_REACHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.ActionEngine;
import jalse.actions.Actions;
import jalse.actions.ForkJoinActionEngine;
import jalse.entities.EntityVisitor.EntityVisitResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A {@link EntityFactory} implementation that creates/kills {@link DefaultEntity}. Default entity
 * factory can have a total entity limit set. When this factory kills an entity it will kill the
 * entity tree under it (can only kill entities his factory has created).<br>
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
    }

    @Override
    public boolean exportEntity(final Entity e) {
	if (!entityIDs.remove(e.getID())) {
	    return false;
	}

	final ActionEngine emptyEngine = Actions.emptyActionEngine();

	final DefaultEntity de = (DefaultEntity) e;
	de.cancelAllScheduledForActor();
	de.setEngine(emptyEngine);
	de.setContainer(null); // Remove parent reference.

	Entities.walkEntityTree(e, vi -> {
	    final DefaultEntity dvi = (DefaultEntity) vi;
	    dvi.cancelAllScheduledForActor();
	    dvi.setEngine(emptyEngine);

	    return EntityVisitResult.CONTINUE;
	});

	return true;
    }

    /**
     * Gets the associated engine.
     *
     * @return Action engine.
     */
    public ActionEngine getEngine() {
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
    public boolean importEntity(final Entity e, final EntityContainer container) {
	if (!entityIDs.add(e.getID())) {
	    return false;
	}

	final DefaultEntity de = (DefaultEntity) e;
	de.setEngine(engine);
	de.setContainer(container);

	Entities.walkEntityTree(de, vi -> {
	    ((DefaultEntity) vi).setEngine(engine);
	    return EntityVisitResult.CONTINUE;
	});

	return false;
    }

    @Override
    public boolean killEntity(final Entity e) {
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
    }

    @Override
    public DefaultEntity newEntity(final UUID id, final EntityContainer container) {
	if (entityCount >= entityLimit) {
	    throwRE(ENTITY_LIMIT_REACHED);
	}

	if (!entityIDs.add(id)) { // Unique only
	    throwRE(ENTITY_ALREADY_ASSOCIATED);
	}

	final DefaultEntity e = new DefaultEntity(id, this, container);
	e.setEngine(engine);
	e.markAsAlive();

	entityCount++;

	return e;
    }

    @Override
    public void setEngine(final ActionEngine engine) {
	this.engine = requireNotStopped(engine);
    }
}