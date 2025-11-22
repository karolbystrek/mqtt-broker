package com.mqtt.broker.service;

import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
public class MqttPacketSender {

    private final MqttPacketEncoder encoder;

    public void send(SocketChannel channel, MqttPacket packet) {
        var encodedPacket = encoder.encode(packet);
        try {
            var bufferToSend = encodedPacket.duplicate(); 
            
            while (bufferToSend.hasRemaining()) {
                channel.write(bufferToSend);
            }
        } catch (IOException e) {
            log.error("Failed to send packet to client {}: {}", channel, e.getMessage());
        }
    }
}
