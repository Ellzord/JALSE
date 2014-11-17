package org.jalse.listeners;

import java.util.UUID;

public interface ClusterListener {

    void clusterCreated(UUID id);

    void clusterKilled(UUID id);
}
