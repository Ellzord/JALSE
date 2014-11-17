package org.jalse.net;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Server {

    ClientInfo getClient(UUID id);

    Set<ClientInfo> getClients();

    void host();

    boolean isHosting();

    void receive(ClientInfo client, JALSEOperation op, List<?> data);

    void send(ClientInfo client, JALSEOperation op, List<?> data);

    void unhost();
}
