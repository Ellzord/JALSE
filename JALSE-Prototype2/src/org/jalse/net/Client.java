package org.jalse.net;

import java.util.List;

public interface Client extends Permissionable {

    void connect();

    void disconnect();

    boolean isConnected();

    void receive(JALSEOperation op, List<?> data);

    void send(JALSEOperation op, List<?> data);
}
