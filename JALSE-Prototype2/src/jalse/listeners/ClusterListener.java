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
     * @param c
     *            Newly created cluster.
     */
    void clusterCreated(Cluster c);

    /**
     * Triggered on cluster death.
     *
     * @param c
     *            Newly deceased cluster.
     */
    void clusterKilled(Cluster c);
}
