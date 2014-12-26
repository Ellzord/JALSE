package jalse.wrappers;

import jalse.actions.Scheduler;
import jalse.attributes.Attributable;
import jalse.tags.Taggable;

import java.util.UUID;

public interface AgentWrapper extends Attributable, Taggable, Scheduler<AgentWrapper> {

    UUID getID();

    boolean kill();
}
