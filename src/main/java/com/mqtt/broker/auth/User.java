package com.mqtt.broker.auth;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
public record User(
        String username,
        String password,
        List<TopicPermission> permissions
) {

    public record TopicPermission(
            String topic,
            PermissionLevel access
    ) {

        public enum PermissionLevel {
            READ,
            WRITE,
            READ_WRITE;

            public boolean canRead() {
                return this == READ || this == READ_WRITE;
            }

            public boolean canWrite() {
                return this == WRITE || this == READ_WRITE;
            }
        }
    }
}
