package org.jalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jalse.attributes.Attribute;
import org.jalse.listeners.AgentListener;
import org.jalse.listeners.AttributeListener;
import org.jalse.misc.JALSEException;
import org.jalse.wrappers.AgentWrapper;
import org.jalse.wrappers.Wrappers;

public class Cluster extends Core<Cluster> {

    private final Set<AgentListener> agentListeners;
    private final Map<UUID, Agent> agents;
    private final Map<Class<?>, Set<Supplier<?>>> listenerSuppliers;

    protected Cluster(final JALSE jalse, final UUID id) {

	super(jalse, id);

	agents = new ConcurrentHashMap<>();

	listenerSuppliers = new HashMap<>();
	agentListeners = new CopyOnWriteArraySet<>();
    }

    public boolean addAgentListener(final AgentListener listener) {

	return agentListeners.add(Objects.requireNonNull(listener));
    }

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
    public <T extends Attribute> Set<Supplier<AttributeListener<T>>> getListenerSuppliers(final Class<T> attr) {

	Set<Supplier<?>> suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	return suppliers != null ? Collections
		.unmodifiableSet((Set<? extends Supplier<AttributeListener<T>>>) suppliers) : Collections.emptySet();
    }

    public boolean kill() {

	return jalse.killCluster(id);
    }

    public boolean killAgent(final UUID id) {

	Agent killed;

	if ((killed = agents.remove(id)) != null) {

	    killed.cancelTasks();

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

	Set<Entry<Class<?>, Set<Supplier<?>>>> entrySet;

	synchronized (listenerSuppliers) {

	    entrySet = new HashSet<>(listenerSuppliers.entrySet());
	}

	for (final Entry<Class<?>, Set<Supplier<?>>> entry : entrySet) {

	    final Class<?> clazz = entry.getKey();

	    for (final Supplier<?> supplier : entry.getValue()) {

		agent.addListener0(clazz, supplier.get());
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

    public <T extends Attribute> boolean removeListenerSupplier(final Class<T> attr,
	    final Supplier<AttributeListener<T>> supplier) {

	Set<Supplier<?>> suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	boolean removed = false;

	if (suppliers != null) {

	    removed = suppliers.remove(supplier);

	    synchronized (listenerSuppliers) {

		if (suppliers.isEmpty()) {

		    listenerSuppliers.remove(attr);
		}
	    }
	}

	return removed;
    }

    public boolean transferAgent(final Cluster cluster, final UUID id) {

	throw new UnsupportedOperationException(); // TODO
    }
}
