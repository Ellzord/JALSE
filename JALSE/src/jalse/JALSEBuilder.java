package jalse;

/**
 * A builder for JALSE. Each method in this builder can be chained. Any
 * parameter not supplied will be defaulted to what is specified in the
 * {@code DEFAULT_} fields.
 *
 * @author Elliot Ford
 * 
 * @see #DEFAULT_TPS
 * @see #DEFAULT_TOTAL_THREADS
 * @see #DEFAULT_CLUSTER_LIMIT
 * @see #DEFAULT_AGENT_LIMIT
 *
 */
public class JALSEBuilder {

    public static final int DEFAULT_TPS = 30;
    public static final int DEFAULT_TOTAL_THREADS = Integer.MAX_VALUE - 1;
    public static final int DEFAULT_CLUSTER_LIMIT = Integer.MAX_VALUE;
    public static final int DEFAULT_AGENT_LIMIT = Integer.MAX_VALUE;

    private int agentLimit;
    private int clusterLimit;
    private int totalThreads;
    private final int tps;

    /**
     * Creates a new builder with only default values.
     */
    public JALSEBuilder() {

	this(DEFAULT_TPS);
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
	clusterLimit = DEFAULT_CLUSTER_LIMIT;
	agentLimit = DEFAULT_AGENT_LIMIT;
    }

    /**
     * Creates an instance of JALSE with the specified parameters.
     *
     * @return Newly created JALSE.
     */
    public JALSE create() {

	return new JALSE(tps, totalThreads, clusterLimit, agentLimit);
    }

    /**
     * Sets the agent limit parameter.
     *
     * @param agentLimit
     *            Maximum agent limited.
     * @return This builder.
     */
    public JALSEBuilder setAgentLimit(final int agentLimit) {

	this.agentLimit = agentLimit;

	return this;
    }

    /**
     * Sets the cluster limit parameter.
     *
     * @param clusterLimit
     *            Maximum cluster limited.
     * @return This builder.
     */
    public JALSEBuilder setClusterLimit(final int clusterLimit) {

	this.clusterLimit = clusterLimit;

	return this;
    }

    /**
     * Sets the total threads parameter.
     *
     * @param totalThreads
     *            Total threads used by the engine.
     * @return This builder.
     */
    public JALSEBuilder setTotalThreads(final int totalThreads) {

	this.totalThreads = totalThreads;

	return this;
    }
}
