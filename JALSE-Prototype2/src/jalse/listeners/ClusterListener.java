package jalse.listeners;

import jalse.Cluster;

import java.util.UUID;

/**
 * Listener for {@link Cluster} creation and death.
 *
 * @author Elliot Ford
 *
 */
public interface ClusterListener {

    /**
     * Triggered on cluster creation.
     *
     * @param id
     *            Identifier of newly created cluster.
     */
    void clusterCreated(UUID id);

    /**
     * Triggered on cluster death.
     *
     * @param id
     *            Identifier of newly deceased cluster.
     */
    void clusterKilled(UUID id);
}
