package jalse.agents;

import jalse.actions.Scheduler;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;
import jalse.listeners.AttributeListener;
import jalse.misc.Identifiable;
import jalse.tags.Taggable;

/**
 * Agent plays the greatest role in the overall data model. An agent is
 * representative of a single entity with a defined identity. Agent can be
 * considered the lowest level child of the JALSE framework. Agents have
 * {@link Attribute} as well as {@link AttributeListener} for trigger code upon
 * add, removal or change of those attributes. Agents can be wrapped to a
 * specific agent type as long as the inheriting interface follows what is
 * outlined in {@link Agents}.
 * 
 * @author Elliot Ford
 * 
 * @see Agents#wrap(Agent, Class)
 *
 */
public interface Agent extends Identifiable, Attributable, Taggable, Scheduler<Agent> {

    /**
     * Kills the agent.
     *
     * @return Whether the agent was alive.
     */
    boolean kill();

    /**
     * Whether the agent is alive.
     *
     * @return Whether the agent is associated to a cluster.
     */
    boolean isAlive();
}