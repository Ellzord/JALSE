package jalse;

import jalse.agents.Agent;
import jalse.tags.Parent;

import java.util.UUID;

class DefaultAgent extends Core<JALSE, Agent> implements Agent {

    private final Cluster cluster;

    DefaultAgent(final UUID id, final Cluster cluster) {

	super(id, cluster.engine);

	this.cluster = cluster;

	tags.add(new Parent(cluster.id));
    }

    @Override
    public boolean kill() {

	tags.removeOfType(Parent.class);

	return cluster.killAgent(id);
    }

    @Override
    public boolean isAlive() {

	return isAttached();
    }
}
