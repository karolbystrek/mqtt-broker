package com.mqtt.broker.service;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.trie.RetainedMessage;
import com.mqtt.broker.trie.strategy.RetainedMessageLookupStrategy;
import com.mqtt.broker.trie.strategy.SubscriberLookupStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.mqtt.broker.packet.MqttQoS.AT_MOST_ONCE;

@Slf4j
@RequiredArgsConstructor
public class MessageDeliveryService {

    private final BrokerContext context;
    private final MqttPacketEncoder packetEncoder = new MqttPacketEncoder();

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
            var topic = packet.getVariableHeader().topicName();
            RetainedMessage retainedMessage = null;
            if (packet.getPayload() != null && packet.getPayload().length > 0) {
                log.info("Retaining message for topic: {}, QoS: {}", topic, packet.getQosLevel());
                retainedMessage = new RetainedMessage(packet.getPayload(), packet.getQosLevel());
            } else {
                log.info("Clearing retained message for topic: {}", topic);
            }

            String[] levels = topic.split("/");
            var strategy = new RetainedMessageLookupStrategy(levels, retainedMessage);
            context.getRetainedMessageTree().perform(strategy);
        }

        var publishPacket = getPublishPacket(packet);

        forwardToSubscribers(publishPacket);
    }

    private PublishPacket getPublishPacket(PublishPacket packet) {
        PublishPacket livePacket = packet;
        if (packet.isRetain()) {
            byte flags = packet.getFixedHeader().flags();
            flags &= (byte) 0b1111_1110;

            var newFixedHeader = new MqttFixedHeader(
                    packet.getFixedHeader().packetType(),
                    flags,
                    packet.getFixedHeader().remainingLength());
            livePacket = new PublishPacket(newFixedHeader, packet.getVariableHeader(),
                    packet.getPayload());
        }
        return livePacket;
    }

    public void dispatchPendingMessages(SocketChannel clientChannel, Session session) {
        session.getPendingMessages().forEach(packet -> send(clientChannel, packet));
        session.clearPendingMessages();
    }

    private void forwardToSubscribers(PublishPacket packet) {
        var topic = packet.getVariableHeader().topicName();
        var subscribedClientIds = new CopyOnWriteArraySet<String>();

        String[] levels = topic.split("/");
        var strategy = new SubscriberLookupStrategy(levels, subscribedClientIds);
        context.getSubscriptionTree().perform(strategy);

        if (subscribedClientIds.isEmpty()) {
            return;
        }

        subscribedClientIds.forEach(clientId -> routeMessageToClient(clientId, packet));
    }

    private void routeMessageToClient(String clientId, PublishPacket packet) {
        SocketChannel channel = context.getClientChannel(clientId);

        if (channel != null) {
            send(channel, packet);
        } else {
            queueMessageForOfflineClient(clientId, packet);
        }
    }

    private void queueMessageForOfflineClient(String clientId, PublishPacket packet) {
        Session persistentSession = context.getPersistentSession(clientId);

        if (persistentSession == null) {
            return;
        }

        if (packet.getQosLevel() != AT_MOST_ONCE) { // qos 1 and 2 should be queued
            log.info("Queuing PUBLISH packet for offline client: {}", clientId);
            persistentSession.enqueuePendingMessage(packet);
        }
    }
}
