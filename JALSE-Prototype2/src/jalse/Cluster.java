package jalse;

import static jalse.agents.Agents.wrap;
import static jalse.misc.JALSEExceptions.AGENT_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.agents.Agent;
import jalse.attributes.Attribute;
import jalse.listeners.AgentListener;
import jalse.listeners.AttributeListener;
import jalse.misc.ListenerSet;

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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cluster extends Core<JALSE, Cluster> {

    private final ListenerSet<AgentListener> agentListeners;
    private final Map<UUID, DefaultAgent> agents;
    private final Map<Class<?>, Set<Supplier<?>>> listenerSuppliers;

    protected Cluster(final JALSE jalse, final UUID id) {

	super(id, jalse);

	agents = new ConcurrentHashMap<>();

	listenerSuppliers = new HashMap<>();
	agentListeners = new ListenerSet<>(AgentListener.class);
    }

    public boolean addAgentListener(final AgentListener listener) {

	return agentListeners.add(listener);
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

	    for (final DefaultAgent a : agents.values()) {

		a.addListener(attr, supplier.get());
	    }
	}

	return added;
    }

    public Stream<Agent> streamAgents() {

	return Collections.<Agent> unmodifiableCollection(agents.values()).stream();
    }

    public <T extends Agent> Stream<T> streamAgents(final Class<T> clazz) {

	return streamAgents().map(a -> wrap(a, clazz));
    }

    public Set<Agent> filterAgents(final Predicate<Agent> filter) {

	return Collections.unmodifiableSet(agents.values().stream().filter(filter).collect(Collectors.toSet()));
    }

    public <T extends Agent> Set<T> filterAgents(final Predicate<T> filter, final Class<T> clazz) {

	return Collections.unmodifiableSet(agents.values().stream().map(a -> wrap(a, clazz)).filter(filter)
		.collect(Collectors.toSet()));
    }

    public Optional<Agent> getAgent(final UUID id) {

	return Optional.ofNullable(agents.get(id));
    }

    public <T extends Agent> Optional<T> getAgent(final UUID id, final Class<T> clazz) {

	final Optional<Agent> agent = getAgent(id);

	return agent.isPresent() ? Optional.of(wrap(agent.get(), clazz)) : Optional.empty();
    }

    public Set<AgentListener> getAgentListeners() {

	return Collections.unmodifiableSet(agentListeners);
    }

    public Set<UUID> getAgents() {

	return Collections.unmodifiableSet(agents.keySet());
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

	return engine.killCluster(id);
    }

    public boolean killAgent(final UUID id) {

	DefaultAgent killed;

	if ((killed = agents.remove(id)) != null) {

	    engine.getAgentCount0().defensiveDecrement();

	    killed.cancelTasks();
	    killed.detatch();

	    agentListeners.getProxy().agentKilled(id);
	}

	return killed != null;
    }

    public UUID newAgent() {

	final UUID id = UUID.randomUUID();

	newAgent(id);

	return id;
    }

    public boolean isAlive() {

	return isAttached();
    }

    public Agent newAgent(final UUID id) {

	engine.getAgentCount0().defensiveIncrement();

	final DefaultAgent agent;

	if (agents.putIfAbsent(id, agent = new DefaultAgent(id, this)) != null) {

	    throwRE(AGENT_ALREADY_ASSOCIATED);
	}

	agentListeners.getProxy().agentCreated(id);

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

    public <T extends Agent> T newAgent(final UUID id, final Class<T> clazz) {

	return wrap(newAgent(id), clazz);
    }

    public boolean removeAgentListener(final AgentListener listener) {

	return agentListeners.remove(listener);
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
}
