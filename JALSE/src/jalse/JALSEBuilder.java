package jalse;

/**
 * A builder for JALSE. Each method in this builder can be chained. Any
 * parameter not supplied will be defaulted to appropriate values (generally
 * {@link Integer#MAX_VALUE}).
 *
 * @author Elliot Ford
 *
 */
public class JALSEBuilder {

    private int agentLimit;
    private int clusterLimit;
    private int totalThreads;
    private final int tps;

    /**
     * Creates a new builder with the given ticks per second.
     *
     * @param tps
     *            Ticks per second.
     */
    public JALSEBuilder(final int tps) {

	this.tps = tps;
	totalThreads = Integer.MAX_VALUE;
	clusterLimit = Integer.MAX_VALUE;
	agentLimit = Integer.MAX_VALUE;
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
