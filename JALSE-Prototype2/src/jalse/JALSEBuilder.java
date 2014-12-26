package jalse;

public class JALSEBuilder {

    private int agentLimit;
    private int clusterLimit;
    private int totalThreads;
    private final int tps;

    public JALSEBuilder(final int tps) {

	this.tps = tps;
	totalThreads = Integer.MAX_VALUE;
	clusterLimit = Integer.MAX_VALUE;
	agentLimit = Integer.MAX_VALUE;
    }

    public JALSE create() {

	return new JALSE(tps, totalThreads, clusterLimit, agentLimit);
    }

    public JALSEBuilder setAgentLimit(final int agentLimit) {

	this.agentLimit = agentLimit;

	return this;
    }

    public JALSEBuilder setClusterLimit(final int clusterLimit) {

	this.clusterLimit = clusterLimit;

	return this;
    }

    public JALSEBuilder setTotalThreads(final int totalThreads) {

	this.totalThreads = totalThreads;

	return this;
    }
}
