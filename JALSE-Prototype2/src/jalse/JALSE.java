package jalse;

import static jalse.misc.JALSEExceptions.CLUSTER_ALREADY_ASSOCIATED;
import static jalse.misc.JALSEExceptions.CLUSTER_LIMIT_REARCHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.actions.Scheduler;
import jalse.agents.Agent;
import jalse.agents.Agents;
import jalse.listeners.ClusterListener;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JALSE extends Engine implements Scheduler<JALSE> {

    private final AtomicInteger agentCount;
    private final int agentLimit;
    private final AtomicInteger clusterCount;
    private final int clusterLimit;
    private final Set<ClusterListener> clusterListeners;
    private final Map<UUID, Cluster> clusters;

    protected JALSE(final int tps, final int totalThreads, final int clusterLimit, final int agentLimit) {

	super(tps, totalThreads);

	if (clusterLimit <= 0 || agentLimit <= 0) {

	    throw new IllegalArgumentException();
	}

	this.clusterLimit = clusterLimit;
	this.agentLimit = agentLimit;

	agentCount = new AtomicInteger();
	clusterCount = new AtomicInteger();

	clusters = new ConcurrentHashMap<>();
	clusterListeners = new CopyOnWriteArraySet<>();
    }

    public boolean addClusterListener(final ClusterListener listener) {

	return clusterListeners.add(Objects.requireNonNull(listener));
    }

    public Set<Cluster> filterClusters(final Predicate<Cluster> filter) {

	return Collections.unmodifiableSet(clusters.values().stream().filter(filter).collect(Collectors.toSet()));
    }

    public int getAgentCount() {

	synchronized (agentCount) {

	    return agentCount.get();
	}
    }

    protected AtomicInteger getAgentCount0() {

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

	    synchronized (clusterCount) {

		clusterCount.decrementAndGet();
	    }

	    killed.cancelTasks();

	    for (final ClusterListener listener : clusterListeners) {

		listener.clusterKilled(id);
	    }
	}

	return killed != null;
    }

    public UUID newCluster() {

	final UUID id = UUID.randomUUID();

	newCluster(id);

	return id;
    }

    public Cluster newCluster(final UUID id) {

	synchronized (clusterCount) {

	    if (clusterCount.get() >= clusterLimit) {

		throwRE(CLUSTER_LIMIT_REARCHED);
	    }

	    clusterCount.incrementAndGet();
	}

	final Cluster cluster;

	if (clusters.putIfAbsent(id, cluster = new Cluster(this, id)) != null) {

	    throwRE(CLUSTER_ALREADY_ASSOCIATED);
	}

	for (final ClusterListener listener : clusterListeners) {

	    listener.clusterCreated(id);
	}

	return cluster;
    }

    public boolean removeAgentListener(final ClusterListener listener) {

	return clusterListeners.remove(Objects.requireNonNull(listener));
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
