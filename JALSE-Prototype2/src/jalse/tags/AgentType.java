package jalse.tags;

import jalse.agents.Agent;
import jalse.agents.Agents;

/**
 * Tag used to help identify an {@link Agent} marked by type.
 *
 * @author Elliot Ford
 *
 */
public class AgentType implements Tag {

    private final Class<? extends Agent> type;

    /**
     * Creates a new agent type tag.
     *
     * @param type
     *            Agent type
     * @throws IllegalArgumentException
     *
     * @see Agents#validateType0(Class)
     */
    public AgentType(final Class<? extends Agent> type) {

	Agents.validateType(type);

	this.type = type;
    }

    /**
     * Gets the agent type.
     *
     * @return Type belonging to an agent.
     */
    public Class<? extends Agent> getType() {

	return type;
    }

    @Override
    public boolean equals(final Object obj) {

	return obj instanceof AgentType && type.equals(((AgentType) obj).type);
    }

    @Override
    public int hashCode() {

	return type.hashCode();
    }

    @Override
    public String toString() {

	return "AgentType [type=" + type.getName() + "]";
    }
}
