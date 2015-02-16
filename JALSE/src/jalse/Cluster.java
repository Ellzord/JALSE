package jalse;

import static jalse.agents.Agents.asType;
import static jalse.misc.JALSEExceptions.AGENT_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.NOT_ALIVE;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.attributes.Attribute;
import jalse.listeners.AgentListener;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;
import jalse.misc.JALSEExceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cluster is a container for {@link Agent} but it also has its own
 * {@link Attribute}. Agents can be created, filtered and killed. Cluster have
 * the ability to add {@link AgentListener} as well as suppliers for
 * {@link AttributeListener} for all agents. {@link Action} can be performed and
 * scheduled on cluster.
 *
 * @author Elliot Ford
 *
 */
public class Cluster extends Core<JALSE, Cluster> {

    private final ListenerSet<AgentListener> agentListeners;
    private final Map<UUID, Agent> agents;
    private final Map<Class<?>, Set<Supplier<?>>> listenerSuppliers;

    /**
     * Creates a new cluster.
     *
     * @param jalse
     *            Parent JALSE engine.
     * @param id
     *            Unique identifier.
     */
    protected Cluster(final JALSE jalse, final UUID id) {

	super(id, jalse);

	agents = new ConcurrentHashMap<>();

	listenerSuppliers = new HashMap<>();
	agentListeners = new ListenerSet<>(AgentListener.class);
    }

    /**
     * Adds a listener for agents.
     *
     * @param listener
     *            Listener to add.
     *
     * @return {@code true} if cluster did not already contain this listener.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#add(Object)
     *
     */
    public boolean addAgentListener(final AgentListener listener) {

	return agentListeners.add(listener);
    }

    /**
     * Adds a listener supplier for all agents.
     *
     * @param attr
     *            Attribute type to assign supplier to.
     * @param supplier
     *            Listener supplier.
     * @return Whether the supplier was not already contained within the
     *         cluster.
     *
     */
    public <T extends Attribute> boolean addListenerSupplier(final Class<T> attr,
	    final Supplier<AttributeListener<T>> supplier) {

	Set<Supplier<?>> suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));

	    if (suppliers == null) {

		listenerSuppliers.put(attr, suppliers = new CopyOnWriteArraySet<>());
	    }
	}

	final boolean added = suppliers.add(supplier);

	if (added) {

	    for (final Agent a : agents.values()) {

		a.addListener(attr, supplier.get());
	    }
	}

	return added;
    }

    /**
     * Provides a stream of agents from the cluster.
     *
     * @return A stream of agents in the cluster.
     */
    public Stream<Agent> streamAgents() {

	return agents.values().stream();
    }

    /**
     * Gets a stream of agents marked with the specified type.
     *
     * @param type
     *            Agent type to check for.
     * @return Set of agents marked with the type.
     *
     * @see Agent#isMarkedAsType(Class)
     * @see Agent#asType(Class)
     */
    public <T extends Agent> Stream<T> streamAgentsOfType(final Class<T> type) {

	return streamAgents().filter(a -> a.isMarkedAsType(type)).map(a -> asType(a, type));
    }

    /**
     * Gets all the agents within the cluster.
     *
     * @return Gets all agents within the cluster.
     */
    public Set<Agent> getAgents() {

	return streamAgents().collect(Collectors.toSet());
    }

    /**
     * Gets all the agents marked with the specified type.
     *
     * @param type
     *            Agent type to check for.
     * @return Set of agents marked with the type.
     *
     * @see Agent#isMarkedAsType(Class)
     * @see Agent#asType(Class)
     */
    public <T extends Agent> Set<T> getAgentsOfType(final Class<T> type) {

	return streamAgentsOfType(type).collect(Collectors.toSet());
    }

    /**
     * Gets the agent with the specified ID.
     *
     * @param id
     *            Unique ID of the agent.
     * @return Gets an Optional of the resulting agent or an empty Optional if
     *         it was not found.
     * @throws NullPointerException
     *             If the ID is null.
     */
    public Optional<Agent> getAgent(final UUID id) {

	return Optional.ofNullable(agents.get(id));
    }

    /**
     * Gets the agent with the specified ID. The agent is wrapped with the
     * supplied agent type.
     *
     * @param id
     *            Unique ID of the agent.
     * @param type
     *            Agent type to wrap to.
     * @return Gets an Optional of the resulting agent or an empty Optional if
     *         it was not found.
     * @throws NullPointerException
     *             If the ID or type are null.
     *
     * @see Agents#asType(Agent, Class)
     */
    public <T extends Agent> Optional<T> getAgentAsType(final UUID id, final Class<T> type) {

	return getAgent(id).map(a -> asType(a, type));
    }

    /**
     * Gets all the agent listeners.
     *
     * @return All the agent listeners.
     */
    public Set<AgentListener> getAgentListeners() {

	return Collections.unmodifiableSet(agentListeners);
    }

    /**
     * Gets the IDs of all the agents within the cluster.
     *
     * @return Set of all agent identifiers.
     */
    public Set<UUID> getAgentIDs() {

	return Collections.unmodifiableSet(agents.keySet());
    }

    /**
     * Gets all listener suppliers assigned to the specified attribute type.
     *
     * @param attr
     *            Attribute type suppliers are assigned to.
     * @return All the suppliers assigned to this attribute type or an empty set
     *         if none are found.
     * @throws NullPointerException
     *             if the attribute type is null.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> Set<Supplier<AttributeListener<T>>> getListenerSuppliers(final Class<T> attr) {

	Set<Supplier<?>> suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	return suppliers != null ? Collections
		.unmodifiableSet((Set<? extends Supplier<AttributeListener<T>>>) suppliers) : Collections.emptySet();
    }

    /**
     * Kills the cluster.
     *
     * @return Whether the cluster was alive.
     */
    public boolean kill() {

	return engine.killCluster(id);
    }

    /**
     * Kills the specified agent.
     *
     * @param id
     *            Agent ID.
     * @return Whether the agent was alive.
     */
    public boolean killAgent(final UUID id) {

	DefaultAgent killed;

	if ((killed = (DefaultAgent) agents.remove(id)) != null) {

	    engine.getAgentCount0().defensiveDecrement();

	    killed.cancelTasks();
	    killed.markAsDead();

	    agentListeners.getProxy().agentKilled(killed);
	}

	return killed != null;
    }

    /**
     * Creates a new agent with a random ID.
     *
     * @return The newly created agent's ID.
     * @throws IllegalStateException
     *             If the agent limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#AGENT_LIMIT_REARCHED
     */
    public Agent newAgent() {

	return newAgent(UUID.randomUUID());
    }

    /**
     * Creates a new agent with a random ID. This agent is wrapped to the
     * specified agent type.
     *
     * @param type
     *            Agent type to wrap to.
     * @return The newly created agent.
     * @throws IllegalStateException
     *             If the agent limit has been reached.
     *
     * @see UUID#randomUUID()
     * @see JALSEExceptions#AGENT_LIMIT_REARCHED
     * @see Agents#asType(Agent, Class)
     */
    public <T extends Agent> T newAgent(final Class<T> type) {

	return newAgent(UUID.randomUUID(), type);
    }

    /**
     * Creates new agent with the specified ID.
     *
     * @param id
     *            Agent ID.
     * @return The newly created agent.
     * @throws IllegalStateException
     *             If the agent limit has been reached.
     * @throws IllegalArgumentException
     *             If the agent ID is already assigned.
     *
     * @see JALSEExceptions#AGENT_LIMIT_REARCHED
     * @see JALSEExceptions#AGENT_ALREADY_ASSOCIATED
     */
    public Agent newAgent(final UUID id) {

	return newAgent0(id, null);
    }

    private Agent newAgent0(final UUID id, final Class<? extends Agent> type) {

	if (!isAlive()) {

	    throwRE(NOT_ALIVE);
	}

	engine.getAgentCount0().defensiveIncrement();

	final DefaultAgent agent;

	if (agents.putIfAbsent(id, agent = new DefaultAgent(id, this)) != null) {

	    throwRE(AGENT_ALREADY_ASSOCIATED);
	}

	agent.markAsAlive();

	if (type != null) {

	    agent.markAsType(type);
	}

	synchronized (listenerSuppliers) {

	    for (final Entry<Class<?>, Set<Supplier<?>>> entry : listenerSuppliers.entrySet()) {

		final Class<?> clazz = entry.getKey();

		for (final Supplier<?> supplier : entry.getValue()) {

		    agent.addListener0(clazz, supplier.get());
		}
	    }
	}

	agentListeners.getProxy().agentCreated(agent);

	return agent;
    }

    /**
     * Creates new agent with the specified ID. This agent is wrapped to the
     * specified agent type.
     *
     * @param id
     *            Agent ID.
     * @param type
     *            Agent type to wrap to.
     * @return The newly created agent.
     * @throws IllegalStateException
     *             If the agent limit has been reached.
     * @throws IllegalArgumentException
     *             If the agent ID is already assigned.
     *
     * @see JALSEExceptions#AGENT_LIMIT_REARCHED
     * @see JALSEExceptions#AGENT_ALREADY_ASSOCIATED
     *
     * @see Agents#asType(Agent, Class)
     */
    public <T extends Agent> T newAgent(final UUID id, final Class<T> type) {

	return asType(newAgent0(id, type), type);
    }

    /**
     * Removes a agent listener.
     *
     * @param listener
     *            Listener to remove.
     *
     * @return {@code true} if the listener was removed.
     * @throws NullPointerException
     *             if listener is null.
     *
     * @see ListenerSet#remove(Object)
     *
     */
    public boolean removeAgentListener(final AgentListener listener) {

	return agentListeners.remove(listener);
    }

    /**
     * Remover a listener supplier.
     *
     * @param attr
     *            Attribute type assigned to the supplier.
     * @param supplier
     *            Listener supplier.
     * @return Whether the supplier was already contained within the cluster.
     */
    public <T extends Attribute> boolean removeListenerSupplier(final Class<T> attr,
	    final Supplier<AttributeListener<T>> supplier) {

	Set<Supplier<?>> suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	boolean removed = false;

	if (suppliers != null) {

	    removed = suppliers.remove(supplier);

	    if (suppliers.isEmpty()) {

		synchronized (listenerSuppliers) {

		    listenerSuppliers.remove(attr);
		}
	    }
	}

	return removed;
    }

    /**
     * Checks whether the agent is alive within the cluster.
     * 
     * @param id
     *            ID of agent.
     * @return {@code true} if the cluster contains the agent and {@code false}
     *         if it does not.
     */
    public boolean isAgentAlive(UUID id) {

	return agents.containsKey(id);
    }

    /**
     * Kills all the agents in the cluster.
     * 
     * @see #killAgent(UUID)
     */
    protected void killAgents() {

	new HashSet<>(agents.keySet()).forEach(this::killAgent);
    }
}
