package org.jalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jalse.attributes.Attribute;
import org.jalse.listeners.AgentListener;
import org.jalse.listeners.SharedAttributeListener;
import org.jalse.misc.JALSEException;
import org.jalse.wrappers.AgentWrapper;
import org.jalse.wrappers.Wrappers;

public class Cluster extends Core<Cluster> {

    private final Set<AgentListener> agentListeners;
    private final Map<UUID, Agent> agents;
    private final Map<Class<?>, Set<SharedAttributeListener<?>>> sharedListeners;

    protected Cluster(final JALSE jalse, final UUID id) {

	super(jalse, id);

	agents = new ConcurrentHashMap<>();
	sharedListeners = new HashMap<>();
	agentListeners = new CopyOnWriteArraySet<>();
    }

    public boolean addAgentListener(final AgentListener listener) {

	return agentListeners.add(Objects.requireNonNull(listener));
    }

    public <T extends Attribute> boolean addSharedListener(final Class<T> attr,
	    final SharedAttributeListener<T> listener) {

	Set<SharedAttributeListener<?>> ls;

	synchronized (sharedListeners) {

	    ls = sharedListeners.get(requireNonNullAttrSub(attr));

	    if (ls == null) {

		sharedListeners.put(attr, ls = new CopyOnWriteArraySet<>());
	    }
	}

	final boolean added = ls.add(listener);

	if (added) {

	    for (final Agent a : agents.values()) {

		a.addListener(attr, SharedAttributeListener.toAttributeListener(listener, a));
	    }
	}

	return added;
    }

    public Set<AgentWrapper> filterAgents(final Predicate<AgentWrapper> filter) {

	return Collections.unmodifiableSet(agents.values().stream().filter(filter).collect(Collectors.toSet()));
    }

    public <T extends AgentWrapper> Set<T> filterAgents(final Predicate<T> filter, final Class<T> clazz) {

	return Collections.unmodifiableSet(agents.values().stream().map(a -> Wrappers.wrap(a, clazz)).filter(filter)
		.collect(Collectors.toSet()));
    }

    public Optional<AgentWrapper> getAgent(final UUID id) {

	return Optional.ofNullable(agents.get(id));
    }

    public <T extends AgentWrapper> Optional<T> getAgent(final UUID id, final Class<T> clazz) {

	final Optional<AgentWrapper> agent = getAgent(id);

	return agent.isPresent() ? Optional.of(Wrappers.wrap(agent.get(), clazz)) : Optional.empty();
    }

    public Set<AgentListener> getAgentListeners() {

	return Collections.unmodifiableSet(agentListeners);
    }

    public Set<UUID> getAgents() {

	return Collections.unmodifiableSet(agents.keySet());
    }

    Set<UUID> getAgents0() {

	return agents.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> Set<SharedAttributeListener<T>> getSharedListeners(final Class<T> attr) {

	Set<SharedAttributeListener<?>> ls;

	synchronized (sharedListeners) {

	    ls = sharedListeners.get(requireNonNullAttrSub(requireNonNullAttrSub(attr)));
	}

	return ls != null ? Collections.unmodifiableSet((Set<? extends SharedAttributeListener<T>>) ls) : Collections
		.emptySet();
    }

    public boolean kill() {

	return jalse.killCluster(id);
    }

    public boolean killAgent(final UUID id) {

	Agent killed;

	if ((killed = agents.remove(id)) != null) {

	    killed.cancelTasks0();

	    for (final AgentListener listener : agentListeners) {

		listener.agentKilled(id);
	    }
	}

	return killed != null;
    }

    public UUID newAgent() {

	final UUID id = UUID.randomUUID();

	newAgent(id);

	return id;
    }

    public AgentWrapper newAgent(final UUID id) {

	AtomicInteger agentCount;

	synchronized (agentCount = jalse.getAgentCount0()) {

	    if (agentCount.get() >= jalse.getAgentLimit()) {

		throw JALSEException.AGENT_LIMIT_REARCHED.get();
	    }

	    agentCount.incrementAndGet();
	}

	final Agent agent;

	if (agents.putIfAbsent(id, agent = new Agent(this, id)) != null) {

	    throw JALSEException.AGENT_ALREADY_ASSOCIATED.get();
	}

	for (final AgentListener listener : agentListeners) {

	    listener.agentCreated(id);
	}

	for (final Entry<Class<?>, Set<SharedAttributeListener<?>>> entry : sharedListeners.entrySet()) {

	    final Class<?> clazz = entry.getKey();

	    for (final SharedAttributeListener<?> listener : entry.getValue()) {

		agent.addListener0(clazz, SharedAttributeListener.toAttributeListener(listener, agent));
	    }
	}

	return agent;
    }

    public <T extends AgentWrapper> T newAgent(final UUID id, final Class<T> clazz) {

	return Wrappers.wrap(newAgent(id), clazz);
    }

    public boolean removeAgentListener(final AgentListener listener) {

	return agentListeners.remove(Objects.requireNonNull(listener));
    }

    public <T extends Attribute> boolean removeSharedListener(final Class<T> attr,
	    final SharedAttributeListener<T> listener) {

	Set<SharedAttributeListener<?>> ls;

	synchronized (sharedListeners) {

	    ls = sharedListeners.get(requireNonNullAttrSub(attr));
	}

	boolean removed = false;

	if (ls != null) {

	    removed = ls.remove(listener);

	    if (removed) {

		for (final Agent a : agents.values()) {

		    a.removeListener0(attr, listener);
		}

		synchronized (sharedListeners) {

		    if (ls.isEmpty()) {

			sharedListeners.remove(attr);
		    }
		}
	    }
	}

	return removed;
    }

    public boolean transferAgent(final Cluster cluster, final UUID id) {

	throw new UnsupportedOperationException(); // TODO
    }
}
