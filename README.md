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
./mvnw exec:java
```

### Windows

```sh
mvnw.cmd exec:java
```

The broker will start and listen for connections on `localhost:1883`.

## Testing the Broker

You can test the broker using a tool like `telnet`.

1. Open a new terminal window.
2. Connect to the broker:
   ```sh
   telnet localhost 1883
   ```
3. Once connected, you can type any message and press Enter. The broker will echo the message back to you.

Example:

```
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Hello
Hello
```

This confirms the broker is running and responding to connections.
