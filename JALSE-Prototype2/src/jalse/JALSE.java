package jalse;

import static jalse.misc.JALSEExceptions.AGENT_LIMIT_REARCHED;
import static jalse.misc.JALSEExceptions.CLUSTER_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.CLUSTER_LIMIT_REARCHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.actions.Scheduler;
import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.listeners.ClusterListener;
import jalse.listeners.ListenerSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JALSE extends Engine implements Scheduler<JALSE> {

    protected class DefensiveAtomicInteger extends AtomicInteger {

	private static final long serialVersionUID = 5685665215057250307L;

	private final Predicate<Integer> predicate;
	private final Supplier<? extends RuntimeException> supplier;

	public DefensiveAtomicInteger(final Predicate<Integer> predicate,
		final Supplier<? extends RuntimeException> supplier) {

	    this.predicate = predicate;
	    this.supplier = supplier;
	}

	private void testPossibleAdd(final int i) {

	    if (!predicate.test(get() + i)) {

		throwRE(supplier);
	    }
	}

	public synchronized void defensiveIncrement() {

	    testPossibleAdd(1);
	    incrementAndGet();
	}

	public synchronized void defensiveDecrement() {

	    testPossibleAdd(-1);
	    decrementAndGet();
	}
    }

    private final DefensiveAtomicInteger agentCount;
    private final int agentLimit;
    private final DefensiveAtomicInteger clusterCount;
    private final int clusterLimit;
    private final ListenerSet<ClusterListener> clusterListeners;
    private final Map<UUID, Cluster> clusters;

    protected JALSE(final int tps, final int totalThreads, final int clusterLimit, final int agentLimit) {

	super(tps, totalThreads);

	if (clusterLimit <= 0 || agentLimit <= 0) {

	    throw new IllegalArgumentException();
	}

	this.clusterLimit = clusterLimit;
	this.agentLimit = agentLimit;

	agentCount = new DefensiveAtomicInteger(i -> i <= this.agentLimit, AGENT_LIMIT_REARCHED);
	clusterCount = new DefensiveAtomicInteger(i -> i <= this.clusterLimit, CLUSTER_LIMIT_REARCHED);

	clusters = new ConcurrentHashMap<>();
	clusterListeners = new ListenerSet<>(ClusterListener.class);
    }

    public boolean addClusterListener(final ClusterListener listener) {

	return clusterListeners.add(listener);
    }

    public Set<Cluster> filterClusters(final Predicate<Cluster> filter) {

	return Collections.unmodifiableSet(clusters.values().stream().filter(filter).collect(Collectors.toSet()));
    }

    public int getAgentCount() {

	synchronized (agentCount) {

	    return agentCount.get();
	}
    }

    protected DefensiveAtomicInteger getAgentCount0() {

	return agentCount;
    }

    public int getAgentLimit() {

	return agentLimit;
    }

    public Set<UUID> getAgents() {

	return Collections.unmodifiableSet(clusters.values().stream().flatMap(c -> c.getAgents().stream())
		.collect(Collectors.toSet()));
    }

    public Stream<Agent> streamAgents() {

	return clusters.values().stream().flatMap(c -> c.streamAgents());
    }

    public <T extends Agent> Stream<T> streamAgents(final Class<T> clazz) {

	return streamAgents().map(a -> Agents.wrap(a, clazz));
    }

    public Optional<Cluster> getCluster(final UUID id) {

	return Optional.ofNullable(clusters.get(id));
    }

    public int getClusterCount() {

	synchronized (clusterCount) {

	    return clusterCount.get();
	}
    }

    public int getClusterLimit() {

	return clusterLimit;
    }

    public Set<ClusterListener> getClusterListeners() {

	return Collections.unmodifiableSet(clusterListeners);
    }

    public Set<UUID> getClusters() {

	return Collections.unmodifiableSet(clusters.keySet());
    }

    public Stream<Cluster> streamClusters() {

	return Collections.<Cluster> unmodifiableCollection(clusters.values()).stream();
    }

    @SuppressWarnings("unchecked")
    public Action<JALSE> getFirstAction() {

	return (Action<JALSE>) getFirstAction0();
    }

    @SuppressWarnings("unchecked")
    public Action<JALSE> getLastAction() {

	return (Action<JALSE>) getLastAction0();
    }

    public boolean hasFirstAction() {

	return getFirstAction0() != null;
    }

    public boolean hasLastAction() {

	return getLastAction0() != null;
    }

    public boolean killCluster(final UUID id) {

	Cluster killed;

	if ((killed = clusters.remove(id)) != null) {

	    clusterCount.defensiveDecrement();

	    killed.detatch();
	    killed.cancelTasks();

	    clusterListeners.getProxy().clusterCreated(id);
	}

	return killed != null;
    }

    public UUID newCluster() {

	final UUID id = UUID.randomUUID();

	newCluster(id);

	return id;
    }

    public Cluster newCluster(final UUID id) {

	clusterCount.defensiveIncrement();

	final Cluster cluster;

	if (clusters.putIfAbsent(id, cluster = new Cluster(this, id)) != null) {

	    throwRE(CLUSTER_ALREADY_ASSOCIATED);
	}

	clusterListeners.getProxy().clusterCreated(id);

	return cluster;
    }

    public boolean removeAgentListener(final ClusterListener listener) {

	return clusterListeners.remove(listener);
    }

    @Override
    public UUID schedule(final Action<JALSE> action, final long initialDelay, final long period, final TimeUnit unit) {

	return schedule0(action, this, initialDelay, period, unit);
    }

    public void setFirstAction(final Action<JALSE> action) {

	setFirstAction0(action, this);
    }

    public void setLastAction(final Action<JALSE> action) {

	setLastAction0(action, this);
    }
}
