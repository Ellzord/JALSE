package jalse;

import jalse.wrappers.AgentWrapper;

import java.util.UUID;

public class Agent extends Core<AgentWrapper> implements AgentWrapper {

    private final Cluster cluster;

    protected Agent(final Cluster cluster, final UUID id) {

	super(cluster.jalse, id);

	this.cluster = cluster;
    }

    @Override
    public boolean kill() {

	return cluster.killAgent(id);
    }
}
