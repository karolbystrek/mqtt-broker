# Development Plan for Building an MQTT Broker

Here is a high-level, phased development plan designed to build your broker incrementally, starting with the most
essential functionalities.

### Development Plan: From TCP Echo Server to MQTT Broker

This plan is broken into phases, with each step representing an atomic piece of functionality.

#### Phase 1: Establish MQTT-Aware Connections

The first priority is to make your server understand the MQTT protocol's basic handshake. Right now, it just echoes
bytes; this phase will teach it to speak the language of MQTT connections.

- [x] **Step 1: Model MQTT Packets**
    * Create Java classes that represent the MQTT packet structure. A good approach is a base `MqttPacket` class
      with
      subclasses for each control packet type (e.g., `ConnectPacket`, `PublishPacket`).
        * Each packet consists of a Fixed Header, a Variable Header, and a Payload. Your models should reflect this
          structure.

- [x] **Step 2: Implement an MQTT Packet Decoder**

    * Replace your `echo` method with a new `handleRead(SelectionKey key)` method that reads bytes from the client.
        * This method's first job is to parse the **Fixed Header**. Read the first byte to identify the packet `type` (
          e.g.,
          `CONNECT` is `1`) and any flags.
    * Next, implement the logic to decode the **Remaining Length**. This is a variable-length integer that tells you
      the
      total size of the rest of the packet. This is a critical step for knowing when you have received a complete
      MQTT
      message.

- [x] **Step 3: Handle the CONNECT Packet**
    * Create a specific handler for the `CONNECT` packet. When your decoder identifies a `CONNECT` packet, it should
      pass the data to this handler.
    * Parse the variable header to get the protocol name, protocol version, connect flags, and Keep Alive interval.
    * Parse the payload to get the `ClientID`.
    * **Validation:** Perform initial checks. The protocol name must be "MQTT" and the version should be 4 (for MQTT
      v3.1.1). If these fail, the connection must be rejected.
- [x] **Step 4: Send the CONNACK Packet and Manage Sessions**
    * Implement logic to create and send a `CONNACK` (Connection Acknowledgment) packet back to the client. If the
      `CONNECT` packet was valid, send a `CONNACK` with a "Connection Accepted" (0x00) return code. Otherwise, send the
      appropriate error code (e.g., "unacceptable protocol version" - 0x01).
    * Create a `Session` object for each successfully connected client. Store these sessions in a `Map`, using the
      `ClientID` as the key. This session object will hold all state for the client, such as their subscriptions.

At the end of this phase, your broker will be able to accept a connection from a standard MQTT client, validate its
`CONNECT` request, and establish a session.

#### Phase 2: Implement Core Messaging

This phase implements the primary purpose of an MQTT broker: receiving messages and fanning them out to interested
subscribers.

- [x] **Step 5: Handle Subscriptions (SUBSCRIBE \& SUBACK)**
    * Implement a handler for the `SUBSCRIBE` packet.
    * Parse its payload to extract the list of topic filters the client is requesting.
    * Store these topic filters inside the client's `Session` object.
    * Send a `SUBACK` packet back to the client to acknowledge the subscription and confirm the granted Quality of
      Service (QoS) levels for each topic.
- [x] **Step 6: Implement Topic Matching Logic**
    * Create a central subscription manager that holds all subscriptions from all active sessions.
    * Write a function that can take a topic name (from a `PUBLISH` packet) and efficiently find all clients whose topic
      filters match it. This logic must correctly handle MQTT wildcards:
        * `+` (single-level wildcard)
        * `#` (multi-level wildcard)
- [x] **Step 7: Handle Publishing (PUBLISH - QoS 0)**
    * Implement a handler for the `PUBLISH` packet. Start by supporting only **QoS 0** ("at most once"), which is the
      simplest fire-and-forget level.
    * When a QoS 0 `PUBLISH` packet arrives, parse its topic name and payload.
    * Use your topic matching logic from the previous step to get a list of all subscribed sessions.
    * For each subscribed client, forward the `PUBLISH` packet to them.

After this phase, you will have a basic but working pub/sub broker. Clients can connect, subscribe to topics, and
publish messages that are correctly delivered to other subscribers.

#### Phase 3: Add Statefulness and Reliability

This phase introduces features that make the broker more robust and compliant with the full MQTT specification.

- [x] **Step 8: Implement Keep Alive (PINGREQ \& PINGRESP)**
    * When processing a `CONNECT` packet, store the client's requested **Keep Alive** interval in its session.
    * In your main server loop, track the time since the last packet was received from each client. If a client is
      silent for more than 1.5 times its Keep Alive interval, you must close the connection.
    * Implement handlers for `PINGREQ` and `PINGRESP`. When you receive a `PINGREQ`, simply respond with a `PINGRESP`.
      Receiving any packet from a client, including `PINGREQ`, resets its inactivity timer.
- [ ] **Step 9: Manage Persistent Sessions (`CleanSession = 0`)**
    * Modify your `CONNECT` handler to check the `CleanSession` flag.
    * If `CleanSession` is `0`, the session is persistent. When the client disconnects, you must preserve its session
      data (especially its subscriptions).
    * When a client reconnects with the same `ClientID` and `CleanSession=0`, you must restore its previous session.
- [ ] **Step 10: Implement Retained Messages**
    * Create a broker-wide storage map (e.g., `Map<String, MqttMessage>`) for retained messages.
    * When a `PUBLISH` packet arrives with the `RETAIN` flag set to `true`, store its message in that map, using the
      topic as the key.
    * When a client subscribes to a topic, immediately check if there is a retained message for a matching topic. If so,
      send that message to the newly subscribed client.

#### Phase 4: User Management and Administration

This final phase addresses your specific requirements for user accounts and topic management.

- [ ] **Step 11: Implement User Authentication**
    * In your `CONNECT` handler, check for the `User Name Flag` and `Password Flag`.
    * If they are set, parse the username and password from the packet's payload.
    * Implement a simple authentication service (it can start as an in-memory map of users) to validate the credentials.
    * If authentication fails, reject the connection with a `CONNACK` code `0x04` (Bad user name or password). This is
      the foundation for pairing devices with user accounts.
- [ ] **Step 12: Build an Administrative API for Topic Management**
    * Expose a simple HTTP API alongside your MQTT server. You can use Java's built-in `HttpServer` or a lightweight
      framework for this.
    * Create endpoints that allow authorized third-party systems to manage topics or user permissions. For example:
        * `POST /api/users`: Create a new user account.
        * `POST /api/permissions`: Grant a user publish/subscribe access to a topic pattern.
    * This API provides the external control you need without cluttering the MQTT protocol itself.
