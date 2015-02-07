package jalse.listeners;

import jalse.Cluster;

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
     *            Newly created cluster.
     */
    void clusterCreated(Cluster id);

    /**
     * Triggered on cluster death.
     *
     * @param id
     *            Newly deceased cluster.
     */
    void clusterKilled(Cluster id);
}
