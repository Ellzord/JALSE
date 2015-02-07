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
 * outlined in {@link Agents}. Agents have can have a number of types (children
 * of {@link Agent}) which can be used to identify a collection of agents with
 * similar state.
 *
 * @author Elliot Ford
 *
 * @see Agents#asType(Agent, Class)
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

    /**
     * Adds the specified type to the agent. If any of the ancestry of this type
     * are not associated to this agent they will also be added.
     *
     * @param type
     *            Agent type to add.
     * @return Whether the type was not associated to the agent.
     */
    boolean markAsType(Class<? extends Agent> type);

    /**
     * Checks whether the agent has the associated type.
     *
     * @param type
     *            Agent type to check.
     * @return Whether the agent was previously associated to the type.
     */
    boolean isMarkedAsType(Class<? extends Agent> type);

    /**
     * Removes the specified type from the agent. If this type is the ancestor
     * of any other types associated to the agent they will be removed.
     *
     * @param type
     *            Agent type to remove.
     * @return Whether the agent was previously associated to the type (or its
     *         any of its children).
     */
    boolean unmarkAsType(Class<? extends Agent> type);

    /**
     * Convenience method for wrapping the agent to a different type.
     *
     * @param type
     *            Agent type to wrap to.
     * @return The wrapped agent.
     *
     * @see Agents#asType(Agent, Class)
     */
    default <T extends Agent> T asType(final Class<T> type) {

	return Agents.asType(this, type);
    }
}