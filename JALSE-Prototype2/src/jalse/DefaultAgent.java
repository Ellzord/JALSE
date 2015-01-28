package jalse;

import jalse.agents.Agent;

import java.util.UUID;

class DefaultAgent extends Core<JALSE, Agent> implements Agent {

    private final Cluster cluster;

    DefaultAgent(final Cluster cluster, final UUID id) {

	super(cluster.engine, id);

	this.cluster = cluster;
    }

    @Override
    public boolean kill() {

	return cluster.killAgent(id);
    }
}
