package com.mqtt.broker.authorization;

import com.mqtt.broker.authorization.User.TopicPermission.PermissionLevel;

public record AuthorizationEntry(String username, PermissionLevel access) {
}
