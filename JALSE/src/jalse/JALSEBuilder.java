package jalse;

import jalse.entities.Entity;

/**
 * A JALSE instance builder. Each method in this builder can be chained. Any parameter not supplied
 * will be defaulted.
 *
 * @author Elliot Ford
 *
 * @see #DEFAULT_TOTAL_THREADS
 * @see #DEFAULT_TOTAL_ENTITY_LIMIT
 *
 */
public class JALSEBuilder {

    /**
     * Creates a single threaded JALSE instance with default values.
     *
     * @param tps
     *            Ticks per second.
     * @return Single threaded JALSE with supplied TPS.
     */
    public static JALSE createSingleThreadedJALSE(final int tps) {
	return new JALSE(tps, 1, DEFAULT_TOTAL_ENTITY_LIMIT);
    }

    /**
     * The default total threads to be used by the engine for performing actions ({@code 10}).
     */
    public static final int DEFAULT_TOTAL_THREADS = 10;

    /**
     * The default {@link Entity} limit ({@code Integer.MAX_VALUE}).
     */
    public static final int DEFAULT_TOTAL_ENTITY_LIMIT = Integer.MAX_VALUE;

    private int totalEntityLimit;
    private int totalThreads;
    private int tps;

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
     * Creates an instance of JALSE with the specified parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE create() {
	return new JALSE(tps, totalThreads, totalEntityLimit);
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
