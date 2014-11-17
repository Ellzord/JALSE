package org.jalse.net;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jalse.Cluster;
import org.jalse.JALSE;

public class RemoteJALSE extends JALSE {

    private RemoteJALSE(final int tps, final int totalThreads, final int clusterLimit, final int agentLimit) {

	super(tps, totalThreads, clusterLimit, agentLimit);
	// TODO Auto-generated constructor stub
    }

    public void authenticate(final List<?> params) {

    }

    public Optional<RemoteCluster> getRemoteCluster(final UUID id) {

	throw new UnsupportedOperationException();

	// return Optional.ofNullable((RemoteCluster)
	// getCluster(id).orElse(null));
    }

    @Override
    public Cluster newCluster(final UUID id) {

	return super.newCluster(id);
    }

    public void setAuthenticator(final PermissionAuthenticator authenticator) {

    }

    public void setHistoryReplayDelay(final int delay) {

	throw new UnsupportedOperationException();
    }
}
