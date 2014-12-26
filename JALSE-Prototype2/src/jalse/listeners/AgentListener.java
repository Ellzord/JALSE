package jalse.listeners;

import java.util.UUID;

public interface AgentListener {

    void agentCreated(UUID id);

    void agentKilled(UUID id);
}
