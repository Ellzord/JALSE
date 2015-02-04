package jalse.listeners;

import jalse.agents.Agent;

import java.util.UUID;

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
     * @param id
     *            Identifier of newly created agent.
     */
    void agentCreated(UUID id);

    /**
     * Triggered on agent death.
     *
     * @param id
     *            Identifier of newly deceased agent.
     */
    void agentKilled(UUID id);
}
