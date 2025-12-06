package com.mqtt.broker.service;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttQoS.AT_MOST_ONCE;

@Slf4j
@RequiredArgsConstructor
public class MessageDispatcher {

    private final BrokerContext context;
    private final MqttPacketSender packetSender;

    public void dispatch(PublishPacket packet) {
        if (packet.isRetain()) {
            context.getTopicTree().retainMessage(
                    packet.getVariableHeader().topicName(),
                    packet.getPayload(),
                    packet.getQosLevel());
        }

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

        forwardToSubscribers(livePacket);
    }

    private void forwardToSubscribers(PublishPacket packet) {
        var topic = packet.getVariableHeader().topicName();
        var subscribedClientIds = context.getTopicTree().getSubscribersFor(topic);

        if (subscribedClientIds.isEmpty()) {
            return;
        }

        subscribedClientIds.forEach(clientId -> routeMessageToClient(clientId, packet));
    }

    private void routeMessageToClient(String clientId, PublishPacket packet) {
        SocketChannel channel = context.getClientChannel(clientId);

        if (channel != null) {
            packetSender.send(channel, packet);
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
