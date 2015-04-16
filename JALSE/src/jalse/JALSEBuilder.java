package jalse;

import static jalse.misc.Identifiable.DUMMY_ID;
import jalse.actions.ActionEngine;
import jalse.actions.ForkJoinActionEngine;
import jalse.actions.ManualActionEngine;
import jalse.actions.ThreadPoolActionEngine;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entity;
import jalse.misc.Identifiable;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

/**
 * A {@link JALSE} instance builder where each method in this builder can be chained. This builder
 * constructs a JALSE instance using the supplied parameters. It does this by using the appropriate
 * {@link ActionEngine} implementation along with a {@link DefaultEntityFactory}. JALSE can still be
 * created without this builder.<br>
 * <br>
 * By default this builder will throw {@link IllegalStateException} as the values must be built. <br>
 * <br>
 * If {@link Entity} must be transfered externally (between two JALSE instances) then unique IDs
 * should be set ({@link UUID#randomUUID()} is used by default).
 *
 * @author Elliot Ford
 *
 *
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 * @see ManualActionEngine
 *
 */
public final class JALSEBuilder {

    private enum EngineType {
	FORKJOIN, THREADPOOL, COMMON, MANUAL, NONE
    }

    /**
     * Creates a common pool JALSE instance (with a random ID and no entity limit).
     *
     * @return Default parallelism JALSE instance.
     *
     * @see #setRandomID()
     * @see #setCommonPoolEngine()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildCommonPoolJALSE() {
	return newBuilder().setRandomID().setNoEntityLimit().setCommonPoolEngine().build();
    }

    /**
     * Builds a manually ticked JALSE instance (with a random ID and no entity limit).
     *
     * @return Manual tick JALSE.
     *
     * @see #setRandomID()
     * @see #setManualEngine()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildManualJALSE() {
	return newBuilder().setRandomID().setNoEntityLimit().setManualEngine().build();
    }

    /**
     * Builds a single threaded JALSE instance (with a random ID and no entity limit).
     *
     * @return Single threaded JALSE instance.
     *
     * @see #setRandomID()
     * @see #setSingleThread()
     * @see #setThreadPoolEngine()
     * @see #setNoEntityLimit()
     */
    public static JALSE buildSingleThreadedJALSE() {
	return newBuilder().setRandomID().setNoEntityLimit().setSingleThread().setThreadPoolEngine().build();
    }

    /**
     * Creates a new builder with defaults.
     *
     * @return New builder.
     */
    public static JALSEBuilder newBuilder() {
	return new JALSEBuilder();
    }

    private UUID id;
    private int parallelism;
    private int totalEntityLimit;
    private EngineType engineType;

    private JALSEBuilder() {
	id = null;
	parallelism = 0;
	totalEntityLimit = 0;
	engineType = EngineType.NONE;
    }

    /**
     * Builds an instance of JALSE with the supplied parameters.
     *
     * @return Newly created JALSE.
     */
    @SuppressWarnings("incomplete-switch")
    public JALSE build() {
	if (engineType == EngineType.NONE) {
	    throw new IllegalStateException("No engine selected");
	} else if (parallelism < 1 && (engineType == EngineType.THREADPOOL || engineType == EngineType.FORKJOIN)) {
	    throw new IllegalStateException("Parallelism must be above one for ThreadPool or ForkJoin engines");
	}

	ActionEngine engine = null;
	switch (engineType) {
	case COMMON:
	    engine = ForkJoinActionEngine.commonPoolEngine();
	    break;
	case MANUAL:
	    engine = new ManualActionEngine();
	    break;
	case THREADPOOL:
	    engine = new ThreadPoolActionEngine(parallelism);
	    break;
	case FORKJOIN:
	    engine = new ForkJoinActionEngine(parallelism);
	    break;
	}

	if (id == null) {
	    throw new IllegalStateException("ID cannot be null");
	}

	if (totalEntityLimit < 1) {
	    throw new IllegalStateException("Entity limit must be above one");
	}

	return new JALSE(id, engine, new DefaultEntityFactory(totalEntityLimit));
    }

    /**
     * Users the common engine based on the common pool.
     *
     * @return This builder.
     *
     * @see ForkJoinPool#commonPool()
     * @see ForkJoinActionEngine#commonPoolEngine()
     */
    public JALSEBuilder setCommonPoolEngine() {
	engineType = EngineType.COMMON;
	return this;
    }

    /**
     * Sets the unique ID for JALSE to a dummy ID.
     *
     * @return This builder.
     *
     * @see Identifiable#DUMMY_ID
     */
    public JALSEBuilder setDummyID() {
	return setID(DUMMY_ID);
    }

    /**
     * Sets fork join engine to be used.
     *
     * @return This builder.
     *
     * @see ForkJoinActionEngine
     */
    public JALSEBuilder setForkJoinEngine() {
	engineType = EngineType.FORKJOIN;
	return this;
    }

    /**
     * Sets the unique ID for JALSE instance.
     *
     * @param id
     *            ID of JALSE.
     * @return This builder.
     */
    public JALSEBuilder setID(final UUID id) {
	this.id = id;
	return this;
    }

    /**
     * Sets the engine to be a manual tick engine.
     *
     * @return This builder.
     */
    public JALSEBuilder setManualEngine() {
	engineType = EngineType.MANUAL;
	return this;
    }

    /**
     * Sets there to be no entity limit.
     *
     * @return This builder.
     *
     * @see Integer#MAX_VALUE
     */
    public JALSEBuilder setNoEntityLimit() {
	return setTotalEntityLimit(Integer.MAX_VALUE);
    }

    /**
     * Sets the parallelism to be utilised by the engine.
     *
     * @param parallelism
     *            Thread parallelism.
     * @return This builder.
     */
    public JALSEBuilder setParallelism(final int parallelism) {
	this.parallelism = parallelism;
	return this;
    }

    /**
     * Sets the parallelism to the available processors.
     *
     * @return This builder.
     *
     * @see Runtime#availableProcessors()
     */
    public JALSEBuilder setParallelismToProcessors() {
	return setParallelism(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Sets the ID to a random one.
     *
     * @return This builder.
     *
     * @see UUID#randomUUID()
     */
    public JALSEBuilder setRandomID() {
	return setID(UUID.randomUUID());
    }

    /**
     * Sets to use a single thread.
     *
     * @return This builder.
     */
    public JALSEBuilder setSingleThread() {
	return setParallelism(1);
    }

    /**
     * Sets thread pool engine to be used.
     *
     * @return This builder.
     *
     * @see ThreadPoolActionEngine
     */
    public JALSEBuilder setThreadPoolEngine() {
	engineType = EngineType.THREADPOOL;
	return this;
    }

    /**
     * Sets the total entity limit parameter.
     *
     * @param totalEntityLimit
     *            Maximum entity limited.
     * @return This builder.
     */
    public JALSEBuilder setTotalEntityLimit(final int totalEntityLimit) {
	this.totalEntityLimit = totalEntityLimit;
	return this;
    }
}
