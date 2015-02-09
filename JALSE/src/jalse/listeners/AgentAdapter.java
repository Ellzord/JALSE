package jalse.listeners;

import jalse.agents.Agent;

/**
 * An abstract adapter for {@link AgentListener}. This is a convenience class
 * for creating agent listeners that may not require a full implementation. All
 * methods implemented by this class are empty.
 *
 * @author Elliot Ford
 *
 */
public abstract class AgentAdapter implements AgentListener {

    @Override
    public void agentCreated(final Agent a) {

    }

    @Override
    public void agentKilled(final Agent a) {

    }
}
