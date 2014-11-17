package org.jalse.net;

import java.util.Map;
import java.util.UUID;

import org.jalse.tags.Permission;

public interface Permissionable {

    Map<UUID, Permission> getAgentPermissions();

    Map<UUID, Permission> getClusterPermissions();

    Permission getJALSEPermission();
}
