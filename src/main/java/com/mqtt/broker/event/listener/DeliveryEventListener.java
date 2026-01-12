package com.mqtt.broker.event.listener;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.BrokerEvent;
import com.mqtt.broker.event.EventListener;
import com.mqtt.broker.event.PublishEvent;
import com.mqtt.broker.packet.PublishPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeliveryEventListener implements EventListener {

    private final BrokerContext context;

    @Override
    public void onEvent(BrokerEvent event) {
        if (event instanceof PublishEvent(var packet)) {
            handlePublishEvent(packet);
        }
    }

    private void handlePublishEvent(PublishPacket packet) {
        log.info("Handling PublishEvent for packet: {}", packet.variableHeader().packetIdentifier());
        context.getMessageDeliveryService().dispatch(packet);
    }
}
