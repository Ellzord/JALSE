package org.jalse.wrappers;

import java.util.UUID;

import org.jalse.actions.Scheduler;
import org.jalse.attributes.Attributable;
import org.jalse.tags.Taggable;

public interface AgentWrapper extends Attributable, Taggable, Scheduler<AgentWrapper> {

    UUID getID();

    boolean kill();
}
