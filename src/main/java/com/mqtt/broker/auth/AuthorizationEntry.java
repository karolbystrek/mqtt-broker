package com.mqtt.broker.auth;

import com.mqtt.broker.auth.User.TopicPermission.PermissionLevel;

public record AuthorizationEntry(String username, PermissionLevel access) {
}
