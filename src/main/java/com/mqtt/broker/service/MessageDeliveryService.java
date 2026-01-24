package com.mqtt.broker.service;

import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.repository.RetainedMessageRepository;
import com.mqtt.broker.repository.SubscriptionRepository;
import com.mqtt.broker.session.Session;
import com.mqtt.broker.session.SessionManager;
import com.mqtt.broker.trie.TopicPath;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.mqtt.broker.packet.MqttQoS.AT_MOST_ONCE;

@Slf4j
@RequiredArgsConstructor
public class MessageDeliveryService {

    private final MqttPacketEncoder packetEncoder = new MqttPacketEncoder();

    private final SessionManager sessionManager;
    private final SubscriptionRepository subscriptionRepository;
    private final RetainedMessageRepository retainedMessageRepository;

    public void send(SocketChannel channel, MqttPacket packet) {
        var encodedPacketBuffer = packetEncoder.encode(packet);
        synchronized (channel) {
            try {
                var bufferToSend = encodedPacketBuffer.duplicate();

                while (bufferToSend.hasRemaining()) {
                    channel.write(bufferToSend);
                }
            } catch (IOException e) {
                log.error("Failed to send packet to client {}: {}", channel, e.getMessage());
            }
        }
    }

    public void dispatch(PublishPacket packet) {
        if (packet.isRetain()) {
            var topic = packet.variableHeader().topicName();
            RetainedMessage retainedMessage = null;
            if (packet.payload() != null && packet.payload().length > 0) {
                log.info("Retaining value for topic: {}, QoS: {}", topic, packet.getQosLevel());
                retainedMessage = new RetainedMessage(packet.payload(), packet.getQosLevel());
            } else {
                log.info("Clearing retained value for topic: {}", topic);
            }

            retainedMessageRepository.add(TopicPath.parse(topic), retainedMessage);
        }

        var publishPacket = getPublishPacket(packet);

        forwardToSubscribers(publishPacket);
    }

    private PublishPacket getPublishPacket(PublishPacket packet) {
        PublishPacket livePacket = packet;
        if (packet.isRetain()) {
            byte flags = packet.fixedHeader().flags();
            flags &= (byte) 0b1111_1110;

            var newFixedHeader = new MqttFixedHeader(
                    packet.fixedHeader().packetType(),
                    flags,
                    packet.fixedHeader().remainingLength());
            livePacket = new PublishPacket(newFixedHeader, packet.variableHeader(),
                    packet.payload());
        }
        return livePacket;
    }

    public void dispatchPendingMessages(SocketChannel clientChannel, Session session) {
        session.getPendingMessages().forEach(packet -> send(clientChannel, packet));
        session.clearPendingMessages();
    }

    private void forwardToSubscribers(PublishPacket packet) {
        var topic = packet.variableHeader().topicName();
        var subscribedClientIds = new CopyOnWriteArraySet<String>();

        subscriptionRepository.findSubscribers(TopicPath.parse(topic), subscribedClientIds);

        if (subscribedClientIds.isEmpty()) {
            return;
        }

        subscribedClientIds.forEach(clientId -> routeMessageToClient(clientId, packet));
    }

    private void routeMessageToClient(String clientId, PublishPacket packet) {
        SocketChannel channel = sessionManager.getClientChannel(clientId);

        if (channel != null) {
            send(channel, packet);
        } else {
            queueMessageForOfflineClient(clientId, packet);
        }
    }

    private void queueMessageForOfflineClient(String clientId, PublishPacket packet) {
        Session persistentSession = sessionManager.getPersistentSession(clientId);

        if (persistentSession == null) {
            return;
        }

        if (packet.getQosLevel() != AT_MOST_ONCE) { // qos 1 and 2 should be queued
            log.info("Queuing PUBLISH packet for offline client: {}", clientId);
            persistentSession.enqueuePendingMessage(packet);
        }
    }
}
