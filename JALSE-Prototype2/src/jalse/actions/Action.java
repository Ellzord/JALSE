package jalse.actions;

import jalse.Cluster;
import jalse.JALSE;
import jalse.TickInfo;
import jalse.agents.Agent;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;
import jalse.misc.Identifiable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Action<T> {

    default Optional<Agent> anyAgent(final Cluster cluster) {

	return cluster.streamAgents().findAny();
    }

    default <S extends Agent> Optional<S> anyAgent(final Cluster cluster, final Class<S> clazz) {

	return cluster.streamAgents(clazz).findAny();
    }

    default Optional<Cluster> anyCluster(final JALSE jalse) {

	return jalse.streamClusters().findAny();
    }

    default <S extends Attributable, U extends Attribute> Predicate<S> notPresent(final Class<U> attr) {

	return c -> c.getAttribute(attr).isPresent();
    }

    default <S extends Attributable, U extends Attribute> Predicate<S> isPresent(final Class<U> attr) {

	return this.<S, U> notPresent(attr).negate();
    }

    default <S extends Identifiable> Predicate<S> isID(final UUID id) {

	return c -> c.getID().equals(id);
    }

    default Set<Agent> filterAgents(final JALSE jalse, final Predicate<Cluster> clusterFilter,
	    final Predicate<Agent> agentFilter) {

	return jalse.filterClusters(clusterFilter).stream().flatMap(c -> c.filterAgents(agentFilter).stream())
		.collect(Collectors.toSet());
    }

    default <S extends Agent> Set<S> filterAgents(final JALSE jalse, final Predicate<Cluster> clusterFilter,
	    final Predicate<S> agentFilter, final Class<S> clazz) {

	return jalse.filterClusters(clusterFilter).stream().flatMap(c -> c.filterAgents(agentFilter, clazz).stream())
		.collect(Collectors.toSet());
    }

    default <S extends Identifiable> Predicate<S> notID(final UUID id) {

	return this.<S> isID(id).negate();
    }

    void perform(T actor, TickInfo tick);
}
