package org.jalse.net;

import java.util.Date;
import java.util.UUID;

public interface ClientInfo extends Permissionable {

    void disconnect();

    UUID getID();

    String getIPAddress();

    Date getLastReceivedDate();

    int getPort();

    boolean isConnected();
}
