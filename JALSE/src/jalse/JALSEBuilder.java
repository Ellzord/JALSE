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
 * created without this builder. Any parameter not supplied will be defaulted.
 *
 * @author Elliot Ford
 *
 * @see #DEFAULT_TOTAL_THREADS
 * @see #DEFAULT_TOTAL_ENTITY_LIMIT
 * @see ContinuousActionEngine
 * @see ManualActionEngine
 *
 */
public class JALSEBuilder {

    /**
     * The default total threads to be used by the engine for performing actions ({@code 10}).
     */
    public static final int DEFAULT_TOTAL_THREADS = 10;

    /**
     * The default {@link Entity} limit ({@code Integer.MAX_VALUE}).
     */
    public static final int DEFAULT_TOTAL_ENTITY_LIMIT = Integer.MAX_VALUE;

    private static final int MANUAL_TPS = 0;

    /**
     * Builds a single threaded JALSE instance with default values.
     *
     * @param tps
     *            Ticks per second.
     * @return Single threaded JALSE with supplied TPS.
     */
    public static JALSE buildSingleThreadedJALSE(final int tps) {
	return new JALSE(new ContinuousActionEngine(tps, DEFAULT_TOTAL_THREADS), new DefaultEntityFactory(
		DEFAULT_TOTAL_ENTITY_LIMIT));
    }

    private int tps;
    private int totalEntityLimit;
    private int totalThreads;

    /**
     * Creates a new builder set to manual tick.
     */
    public JALSEBuilder() {
	this(MANUAL_TPS);
    }

    /**
     * Creates a new builder with the given ticks per second and default values.
     *
     * @param tps
     *            Ticks per second.
     */
    public JALSEBuilder(final int tps) {
	this.tps = tps;
	totalThreads = DEFAULT_TOTAL_THREADS;
	totalEntityLimit = DEFAULT_TOTAL_ENTITY_LIMIT;
    }

    /**
     * Builds an instance of JALSE with the supplied parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE build() {
	final ActionEngine engine = tps > MANUAL_TPS ? new ContinuousActionEngine(tps, totalThreads)
		: new ManualActionEngine();
	return new JALSE(engine, new DefaultEntityFactory(totalEntityLimit));
    }

    /**
     * Sets the engine to be a manual tick engine.
     * 
     * @return This builder.
     */
    public JALSEBuilder setManualTick() {
	return setTPS(MANUAL_TPS);
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
