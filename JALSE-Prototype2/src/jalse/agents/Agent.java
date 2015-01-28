package jalse.agents;

import jalse.actions.Scheduler;
import jalse.attributes.Attributable;
import jalse.misc.Identifiable;
import jalse.tags.Taggable;

public interface Agent extends Identifiable, Attributable, Taggable, Scheduler<Agent> {

    boolean kill();
}