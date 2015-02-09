package jalse.listeners;

import jalse.Cluster;

/**
 * An abstract adapter for {@link ClusterListener}. This is a convenience class
 * for creating cluster listeners that may not require a full implementation.
 * All methods implemented by this class are empty.
 *
 * @author Elliot Ford
 *
 */
public abstract class ClusterAdapter implements ClusterListener {

    @Override
    public void clusterCreated(final Cluster c) {

    }

    @Override
    public void clusterKilled(final Cluster c) {

    }
}
