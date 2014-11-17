package org.jalse.net;

import java.util.List;

import org.jalse.attributes.Attribute;
import org.jalse.tags.Permission;

public interface PermissionAuthenticator {

    Permissionable authenticate(ClientInfo client, List<?> params);

    <T extends Attribute> Permission getDefaultPermission(Class<T> attr);

    <T extends Attribute> void setDefaultPermission(Class<T> attr, Permission p);
}
