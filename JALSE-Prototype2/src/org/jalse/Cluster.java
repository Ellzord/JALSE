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

    private class Suppliers {

	private final Map<Supplier<?>, Set<Map<UUID, AttributeListener<?>>>> suppliersAndChildren;

	private Suppliers() {

	    suppliersAndChildren = new ConcurrentHashMap<>();
	}

	public void add(final Supplier<?> supplier) {

	    // TODO
	}

	public void addChild(final Supplier<?> supplier, final UUID owner, final AttributeListener<?> child) {

	    // TODO
	}

	public boolean contains(final Supplier<?> supplier) {

	    return suppliersAndChildren.containsKey(supplier);
	}

	public Set<Supplier<?>> getSuppliers() {

	    return suppliersAndChildren.keySet();
	}

	public boolean isEmpty() {

	    return suppliersAndChildren.isEmpty();
	}

	public void remove(final Supplier<?> supplier) {

	    // TODO
	}

	public AttributeListener<?> removeChild(final Supplier<?> supplier, final UUID owner) {

	    return null; // TODO
	}

	public void removeChildren(final UUID owner) {

	    // TODO
	}
    }

    private final Set<AgentListener> agentListeners;
    private final Map<UUID, Agent> agents;
    private final Map<Class<?>, Suppliers> listenerSuppliers;

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

	Suppliers suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));

	    if (suppliers == null) {

		listenerSuppliers.put(attr, suppliers = new Suppliers());
	    }
	}

	final boolean add = !suppliers.contains(supplier);

	if (add) {

	    suppliers.add(supplier);

	    for (final Agent a : agents.values()) {

		final AttributeListener<T> listener = supplier.get();

		if (a.addListener(attr, listener)) {

		    suppliers.addChild(supplier, a.getID(), listener);
		}
	    }
	}

	return add;
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

	Suppliers suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	return suppliers != null ? Collections
		.unmodifiableSet((Set<? extends Supplier<AttributeListener<T>>>) suppliers.getSuppliers())
		: Collections.emptySet();
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

	    Set<Suppliers> values;

	    synchronized (listenerSuppliers) {

		values = new HashSet<>(listenerSuppliers.values());
	    }

	    for (final Suppliers suppliers : values) {

		suppliers.removeChildren(id);
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

	Set<Entry<Class<?>, Suppliers>> entrySet;

	synchronized (listenerSuppliers) {

	    entrySet = new HashSet<>(listenerSuppliers.entrySet());
	}

	for (final Entry<Class<?>, Suppliers> entry : entrySet) {

	    final Class<?> clazz = entry.getKey();
	    final Suppliers suppliers = entry.getValue();

	    for (final Supplier<?> supplier : suppliers.getSuppliers()) {

		final AttributeListener<?> listener = (AttributeListener<?>) supplier.get();

		agent.addListener0(clazz, listener);
		suppliers.addChild(supplier, agent.getID(), listener);
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

	Suppliers suppliers;

	synchronized (listenerSuppliers) {

	    suppliers = listenerSuppliers.get(requireNonNullAttrSub(attr));
	}

	boolean remove = false;

	if (suppliers != null) {

	    if (remove = suppliers.contains(supplier)) {

		for (final Agent a : agents.values()) {

		    final AttributeListener<?> listener = suppliers.removeChild(supplier, a.getID());

		    if (listener != null) {

			a.removeListener0(attr, listener);
		    }
		}

		suppliers.remove(supplier);

		synchronized (listenerSuppliers) {

		    if (suppliers.isEmpty()) {

			listenerSuppliers.remove(attr);
		    }
		}
	    }
	}

	return remove;
    }

    public boolean transferAgent(final Cluster cluster, final UUID id) {

	throw new UnsupportedOperationException(); // TODO
    }
}
