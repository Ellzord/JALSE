package jalse;

import jalse.agents.Agent;
import jalse.tags.Parent;

import java.util.UUID;

class DefaultAgent extends Core<JALSE, Agent> implements Agent {

    private final Cluster cluster;

    DefaultAgent(final Cluster cluster, final UUID id) {

	super(cluster.engine, id);

	this.cluster = cluster;

	tags.add(new Parent(cluster.id));
    }

    @Override
    public boolean kill() {

	tags.remove(Parent.class);

	return cluster.killAgent(id);
    }

    @Override
    public boolean isAlive() {

	return tags.contains(Parent.class);
    }
}
