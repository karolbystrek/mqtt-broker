package com.mqtt.broker.authorization.strategy;

import com.mqtt.broker.authorization.User.TopicPermission.PermissionLevel;
import com.mqtt.broker.config.BrokerConfiguration.DatabaseConfiguration;
import com.mqtt.broker.packet.ConnectPacket;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class DatabaseAuthorizationStrategy implements AuthorizationStrategy {

    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    public DatabaseAuthorizationStrategy(DatabaseConfiguration config) {
        this.url = config.getUrl();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.driver = config.getDriver();

        try {
            Class.forName(driver);
            try (Connection conn = getConnection()) {
                if (conn != null) {
                    log.info("Successfully connected to the database.");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public boolean authenticate(ConnectPacket packet) {
        if (!packet.variableHeader().hasUsername() || !packet.variableHeader().hasPassword()) {
            return false;
        }
        String user = packet.payload().username();
        String pass = packet.payload().password();

        if (user == null || pass == null) {
            return false;
        }

        String query = "SELECT count(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user);
            stmt.setString(2, pass);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Error authenticating user {}", user, e);
        }
        return false;
    }

    @Override
    public boolean canSubscribe(String username, String topicFilter) {
        return checkPermission(username, topicFilter, true);
    }

    @Override
    public boolean canPublish(String username, String topic) {
        return checkPermission(username, topic, false);
    }

    private boolean checkPermission(String username, String topic, boolean isRead) {
        String query = "SELECT p.topic, p.access_level FROM permissions p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE u.username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String allowedTopic = rs.getString("topic");
                    String accessLevelStr = rs.getString("access_level");
                    PermissionLevel level;
                    try {
                        level = PermissionLevel.valueOf(accessLevelStr);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid permission level found in DB: {}", accessLevelStr);
                        continue;
                    }

                    if ((isRead && level.canRead()) || (!isRead && level.canWrite())) {
                        if (matches(allowedTopic, topic)) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error checking permissions for user {}", username, e);
        }
        return false;
    }

    private boolean matches(String rule, String topic) {
        if (rule.equals(topic) || rule.equals("#")) return true;

        String[] ruleLevels = rule.split("/");
        String[] topicLevels = topic.split("/");

        for (int i = 0; i < ruleLevels.length; i++) {
            String ruleLevel = ruleLevels[i];

            if (ruleLevel.equals("#")) {
                return true;
            }

            if (i >= topicLevels.length) {
                return false;
            }

            if (!ruleLevel.equals("+") && !ruleLevel.equals(topicLevels[i])) {
                return false;
            }
        }

        return ruleLevels.length == topicLevels.length;
    }
}
