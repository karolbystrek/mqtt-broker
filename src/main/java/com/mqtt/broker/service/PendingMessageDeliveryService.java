package com.mqtt.broker.service;

import com.mqtt.broker.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
@Slf4j
public class PendingMessageDeliveryService {

    private final MqttPacketSender packetSender;

    public void deliverPendingMessages(SocketChannel clientChannel, Session session) {
        session.getPendingMessagesStream()
                .forEach(packet -> packetSender.send(clientChannel, packet));

        session.clearPendingMessages();
    }
}
