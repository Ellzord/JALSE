package jalse;

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
 * constructs a JALSE instance using the supplied parameters. It does this by creating the
 * appropriate {@link ActionEngine} implementation along with a {@link DefaultEntityFactory}. JALSE
 * can still be created without this builder.<br>
 * <br>
 * Parallelism:<br>
 * If {@code parallelism < 0} (or {@link #setCommonPoolEngine()}) then
 * {@link ForkJoinActionEngine#commonPoolEngine()} is used.<br>
 * If {@code parallelism == 0} (or {@link #setManual()}) then {@link ManualActionEngine} is used.<br>
 * If {@code parallelism == 1} then {@link ThreadPoolActionEngine} will be used (
 * {@code corePoolSize = 1}).<br>
 * If {@code parallelism > 1} then {@link ForkJoinActionEngine} will be used. <br>
 * <br>
 * If {@link Entity} must be transfered extenerally (between two JALSE instances) then uniue IDs
 * should be set.
 *
 * @author Elliot Ford
 *
 * @see #DEFAULT_TOTAL_ENTITY_LIMIT
 * @see #DEFAULT_PARALLELISM
 * @see #DEFAULT_ID
 *
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 * @see ManualActionEngine
 *
 */
public final class JALSEBuilder {

    /**
     * The default parallelism to be utilised by the engine for performing actions (
     * {@code Runtime.getRuntime().availableProcessors()}).
     */
    public static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();

    /**
     * The default {@link Entity} limit ({@code Integer.MAX_VALUE}).
     */
    public static final int DEFAULT_TOTAL_ENTITY_LIMIT = Integer.MAX_VALUE;

    /**
     * The default {@link UUID}.
     *
     * @see Identifiable#DUMMY_ID
     */
    public static final UUID DEFAULT_ID = Identifiable.DUMMY_ID;

    /**
     * Creates a JALSE instance with default parallelism.
     *
     * @return Default parallelism JALSE instance.
     *
     * @see #DEFAULT_PARALLELISM
     * @see #DEFAULT_TOTAL_ENTITY_LIMIT
     */
    public static JALSE buildDefaultJALSE() {
	return newBuilder().build();
    }

    /**
     * Builds a manually ticked JALSE instance ({@code parallelism == 0}).
     *
     * @return Manual tick JALSE.
     *
     * @see #DEFAULT_TOTAL_ENTITY_LIMIT
     */
    public static JALSE buildManualJALSE() {
	return newBuilder().setManual().build();
    }

    /**
     * Builds a single threaded JALSE instance ({@code parallelism == 1}).
     *
     * @return Single threaded JALSE instance.
     *
     * @see #DEFAULT_TOTAL_ENTITY_LIMIT
     */
    public static JALSE buildSingleThreadedJALSE() {
	return newBuilder().setParallelism(1).build();
    }

    /**
     * Creates a new builder with defaults.
     *
     * @return New builder.
     */
    public static JALSEBuilder newBuilder() {
	return new JALSEBuilder();
    }

    private UUID id = DEFAULT_ID;
    private int parallelism = DEFAULT_PARALLELISM;
    private int totalEntityLimit = DEFAULT_TOTAL_ENTITY_LIMIT;

    private JALSEBuilder() {}

    /**
     * Builds an instance of JALSE with the supplied parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE build() {
	final ActionEngine engine;

	if (parallelism < 0) {
	    engine = ForkJoinActionEngine.commonPoolEngine();
	} else if (parallelism == 0) {
	    engine = new ManualActionEngine();
	} else if (parallelism == 1) {
	    engine = new ThreadPoolActionEngine(1);
	} else {
	    engine = new ForkJoinActionEngine(parallelism);
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
	setParallelism(-1);
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
	id = DEFAULT_ID;
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
    public JALSEBuilder setManual() {
	return setParallelism(0);
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
	parallelism = DEFAULT_PARALLELISM;
	return this;
    }

    /**
     * Sets the ID to a random one.
     *
     * @return This builder.
     *
     * @see UUID#randomUUID()
     */
    public JALSEBuilder setRandomID() {
	id = UUID.randomUUID();
	return this;
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
