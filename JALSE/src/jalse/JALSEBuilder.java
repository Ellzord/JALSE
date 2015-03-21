package jalse;

import jalse.actions.ActionEngine;
import jalse.actions.ForkJoinActionEngine;
import jalse.actions.ManualActionEngine;
import jalse.actions.ThreadPoolActionEngine;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entity;

/**
 * A {@link JALSE} instance builder where each method in this builder can be chained. This builder
 * constructs a JALSE instance using the supplied parameters. It does this by creating the
 * appropriate {@link ActionEngine} implementation along with a {@link DefaultEntityFactory}. JALSE
 * can still be created without this builder.<br>
 * <br>
 * Parallelism:<br>
 * If {@code parallelism <= 0}(or {@link #setManual()}) then {@link ManualActionEngine} is used.<br>
 * If {@code parallelism == 1} then {@link ThreadPoolActionEngine} will be used (
 * {@code corePoolSize = 1}).<br>
 * If {@code parallelism > 1} then {@link ForkJoinActionEngine} will be used.
 *
 * @author Elliot Ford
 *
 * @see #DEFAULT_TOTAL_ENTITY_LIMIT
 *
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 * @see ManualActionEngine
 *
 */
public class JALSEBuilder {

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

	if (parallelism <= 0) {
	    engine = new ManualActionEngine();
	} else if (parallelism == 1) {
	    engine = new ThreadPoolActionEngine(1);
	} else {
	    engine = new ForkJoinActionEngine(parallelism);
	}

	return new JALSE(engine, new DefaultEntityFactory(totalEntityLimit));
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
