package jalse.actions;

import jalse.Cluster;
import jalse.JALSE;
import jalse.TickInfo;
import jalse.wrappers.AgentWrapper;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Action<T> {

    default <S extends AgentWrapper> Predicate<S> agentID(final UUID id) {

	return a -> a.getID().equals(id);
    }

    default Optional<AgentWrapper> anyAgent(final Cluster cluster) {

	return cluster.filterAgents(a -> true).stream().findAny();
    }

    default <S extends AgentWrapper> Optional<S> anyAgent(final Cluster cluster, final Class<S> clazz) {

	return cluster.filterAgents(a -> true, clazz).stream().findAny();
    }

    default Optional<Cluster> anyCluster(final JALSE jalse) {

	return jalse.filterClusters(c -> true).stream().findAny();
    }

    default Predicate<Cluster> clusterID(final UUID id) {

	return c -> c.getID().equals(id);
    }

    default Set<AgentWrapper> filterAgents(final JALSE jalse, final Predicate<Cluster> clusterFilter,
	    final Predicate<AgentWrapper> agentFilter) {

	return jalse.filterClusters(clusterFilter).stream().flatMap(c -> c.filterAgents(agentFilter).stream())
		.collect(Collectors.toSet());
    }

    default <S extends AgentWrapper> Set<S> filterAgents(final JALSE jalse, final Predicate<Cluster> clusterFilter,
	    final Predicate<S> agentFilter, final Class<S> clazz) {

	return jalse.filterClusters(clusterFilter).stream().flatMap(c -> c.filterAgents(agentFilter, clazz).stream())
		.collect(Collectors.toSet());
    }

    default <S extends AgentWrapper> Predicate<S> notAgentID(final UUID id) {

	return this.<S> agentID(id).negate();
    }

    default Predicate<Cluster> notClusterID(final UUID id) {

	return clusterID(id).negate();
    }

    void perform(T actor, TickInfo tick);
}
