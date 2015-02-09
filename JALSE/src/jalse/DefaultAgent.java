package jalse;

import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.tags.AgentType;
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

    @Override
    public boolean markAsType(final Class<? extends Agent> type) {

	boolean added = false;

	if (added = tags.add(new AgentType(type))) {

	    for (final Class<? extends Agent> t : Agents.getAncestry(type)) {

		tags.add(new AgentType(t));
	    }
	}

	return added;
    }

    @Override
    public boolean isMarkedAsType(final Class<? extends Agent> type) {

	return tags.contains(new AgentType(type));
    }

    @Override
    public boolean unmarkAsType(final Class<? extends Agent> type) {

	boolean removed;

	if (removed = tags.remove(new AgentType(type))) {

	    for (final AgentType at : tags.getOfType(AgentType.class)) {

		final Class<? extends Agent> t = at.getType();

		if (Agents.isDescendant(t, type)) {

		    tags.remove(at);
		}
	    }
	}

	return removed;
    }
}
