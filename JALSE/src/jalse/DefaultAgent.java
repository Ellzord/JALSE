package jalse;

import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.tags.AgentType;
import jalse.tags.Parent;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	return cluster.isAgentAlive(id);
    }

    @Override
    public boolean markAsType(final Class<? extends Agent> type) {

	boolean result = false;

	if (!isMarkedAsType(type)) {

	    tags.add(new AgentType(type));
	    Agents.getAncestry(type).stream().map(t -> new AgentType(t)).forEach(tags::add);
	    result = true;
	}

	return result;
    }

    @Override
    public boolean isMarkedAsType(final Class<? extends Agent> type) {

	return tags.getOfType(AgentType.class).stream().anyMatch(at -> Agents.isOrDescendant(at.getType(), type));
    }

    @Override
    public boolean unmarkAsType(final Class<? extends Agent> type) {

	final Set<AgentType> descendants = tags.getOfType(AgentType.class).stream()
		.filter(at -> Agents.isOrDescendant(at.getType(), type)).collect(Collectors.toSet());

	descendants.forEach(tags::remove);

	return !descendants.isEmpty();
    }
}
