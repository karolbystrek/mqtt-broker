# mqtt-broker

MQTT broker created for Design Patterns university class

## Development plan can be found in [PLAN.md](PLAN.md)

## Prerequisites

- Java 21 or higher
- Maven

## Running the Broker

To run the broker, execute the following command from the root directory of the project:

### macOS / Linux

```sh
./mvnw clean compile exec:java
```

### Windows

```sh
mvnw.cmd clean compile exec:java
```

The broker will start and listen for connections on `localhost:1883`.

## Testing the Broker

You can test the broker using a tool like MQTTX, which is a cross-platform MQTT desktop client.

### Installing MQTTX

1. Download MQTTX from the official website: [https://mqttx.app/](https://mqttx.app/)
2. Install the application on your operating system.

### Connecting to the Broker with MQTTX

1. **Start the broker** by following the instructions in the "Running the Broker" section.
2. **Open MQTTX**.
3. Click on **"New Connection"**.
4. Configure the connection with the following details:
    - **Name**: `Local Broker` (or any name you prefer)
    - **Host**: `localhost`
    - **Port**: `1883`
    - **MQTT Version**: Select `3.1.1` from the dropdown.
    - All other settings can be left as default.
5. Click **"Connect"**.

If the connection is successful, you will see a "Connected" status in MQTTX. The broker terminal will also print a
message indicating a new client has connected and that a `CONNACK` packet has been sent.

This confirms that the broker is correctly handling the MQTT `CONNECT` packet and establishing a connection.
