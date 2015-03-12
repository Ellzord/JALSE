package jalse;

import jalse.actions.ActionEngine;
import jalse.actions.ContinuousActionEngine;
import jalse.actions.ManualActionEngine;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entity;

/**
 * A {@link JALSE} instance builder where each method in this builder can be chained. This builder
 * constructs a JALSE instance using the supplied parameters. It does this by creating the
 * appropriate {@link ActionEngine} along with a {@link DefaultEntityFactory}. JALSE can still be
 * created without this builder. Any parameter not supplied will be defaulted and setting {@code 0}
 * TPS is equivalent to setting manual tick.
 *
 * @author Elliot Ford
 *
 * @see #DEFAULT_TPS
 * @see #DEFAULT_TOTAL_THREADS
 * @see #DEFAULT_TOTAL_ENTITY_LIMIT
 *
 * @see ContinuousActionEngine
 * @see ManualActionEngine
 *
 */
public class JALSEBuilder {

    /**
     * The default maximum ticks per second to be achieved by the engine ({@code 30}).
     */
    public static final int DEFAULT_TPS = 30;

    /**
     * The default total threads to be used by the engine for performing actions ({@code 10}).
     */
    public static final int DEFAULT_TOTAL_THREADS = 10;

    /**
     * The default {@link Entity} limit ({@code Integer.MAX_VALUE}).
     */
    public static final int DEFAULT_TOTAL_ENTITY_LIMIT = Integer.MAX_VALUE;

    /**
     * Builds a manually ticked JALSE instance with default values.
     *
     * @return Manual tick JALSE.
     */
    public static JALSE buildManualTickJALSE() {
	return newBuilder().setManualTick().build();
    }

    /**
     * Builds a single threaded JALSE instance with default values.
     *
     * @param tps
     *            Ticks per second.
     * @return Single threaded JALSE with supplied TPS.
     */
    public static JALSE buildSingleThreadedJALSE(final int tps) {
	return newBuilder().setTPS(tps).setTotalThreads(1).build();
    }

    /**
     * Creates a new builder set to manual tick with defaults.
     *
     * @return New builder.
     */
    public static JALSEBuilder newBuilder() {
	return new JALSEBuilder();
    }

    private int tps = DEFAULT_TPS;
    private int totalThreads = DEFAULT_TOTAL_THREADS;
    private int totalEntityLimit = DEFAULT_TOTAL_ENTITY_LIMIT;

    private JALSEBuilder() {}

    /**
     * Builds an instance of JALSE with the supplied parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE build() {
	final ActionEngine engine = tps > 0 ? new ContinuousActionEngine(tps, totalThreads) : new ManualActionEngine();
	return new JALSE(engine, new DefaultEntityFactory(totalEntityLimit));
    }

    /**
     * Sets the engine to be a manual tick engine.
     *
     * @return This builder.
     */
    public JALSEBuilder setManualTick() {
	return setTPS(0);
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

    /**
     * Sets the total threads that can be used.
     *
     * @param totalThreads
     *            Total threads used by the engine.
     * @return This builder.
     *
     */
    public JALSEBuilder setTotalThreads(final int totalThreads) {
	this.totalThreads = totalThreads;
	return this;
    }

    /**
     * Sets the ticks per second.
     *
     * @param tps
     *            Ticks per second.
     * @return This builder.
     */
    public JALSEBuilder setTPS(final int tps) {
	this.tps = tps;
	return this;
    }
}
