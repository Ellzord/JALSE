package jalse.listeners;

import java.util.UUID;

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
    public void clusterCreated(final UUID id) {

    }

    @Override
    public void clusterKilled(final UUID id) {

    }
}
