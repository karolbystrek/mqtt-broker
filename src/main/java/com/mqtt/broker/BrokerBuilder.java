package com.mqtt.broker;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.connection.ClientConnection;
import com.mqtt.broker.connection.ServerListener;
import com.mqtt.broker.decoder.MqttPacketDecoder;
import com.mqtt.broker.decoder.ProtocolDecoder;
import com.mqtt.broker.event.EventPublisher;
import com.mqtt.broker.pipeline.Pipeline;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerBuilder {

    private BrokerConfiguration config;
    private BrokerContext context;
    private EventPublisher eventPublisher;
    private Pipeline pipeline;
    private ExecutorService packetExecutor;
    private ProtocolDecoder packetDecoder;

    public BrokerBuilder config(BrokerConfiguration config) {
        this.config = config;
        return this;
    }

    public BrokerBuilder context(BrokerContext context) {
        this.context = context;
        return this;
    }

    public BrokerBuilder eventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        return this;
    }

    public BrokerBuilder pipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public BrokerBuilder packetExecutor(ExecutorService packetExecutor) {
        this.packetExecutor = packetExecutor;
        return this;
    }

    public BrokerBuilder packetDecoder(ProtocolDecoder packetDecoder) {
        this.packetDecoder = packetDecoder;
        return this;
    }

    public Broker build() throws IOException {
        if (config == null) throw new IllegalStateException("BrokerConfiguration is required");
        if (context == null) throw new IllegalStateException("BrokerContext is required");
        if (eventPublisher == null) throw new IllegalStateException("EventPublisher is required");
        if (pipeline == null) throw new IllegalStateException("PacketProcessingPipeline is required");

        if (packetExecutor == null) {
            packetExecutor = Executors.newVirtualThreadPerTaskExecutor();
        }

        if (packetDecoder == null) {
            packetDecoder = new MqttPacketDecoder();
        }

        Selector selector = Selector.open();
        var connections = new ConcurrentHashMap<SocketChannel, ClientConnection>();

        var serverListener = new ServerListener(selector, config, connections);
        ServerSocketChannel serverChannel = serverListener.setup();

        return new Broker(
                context,
                selector,
                serverChannel,
                packetDecoder,
                eventPublisher,
                serverListener,
                packetExecutor,
                pipeline,
                connections
        );
    }
}
