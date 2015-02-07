package jalse.listeners;

import jalse.agents.Agent;

/**
 * Listener for {@link Agent} creation and death.
 *
 * @author Elliot Ford
 *
 */
public interface AgentListener {

    /**
     * Triggered on agent creation.
     *
     * @param a
     *            Newly created agent.
     */
    void agentCreated(Agent a);

    /**
     * Triggered on agent death.
     *
     * @param a
     *            Newly deceased agent.
     */
    void agentKilled(Agent a);
}
