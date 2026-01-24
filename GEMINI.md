# GEMINI.MD: AI Collaboration Guide

This document provides essential context for AI models interacting with this project. Adhering to these guidelines will ensure consistency and maintain code quality.

## 1. Project Overview & Purpose

*   **Primary Goal:** This is a custom, high-performance MQTT 3.1.1 compliant broker implemented in Java. It is designed to demonstrate advanced software design patterns (Reactor, Strategy, Observer, Command, Chain of Responsibility) and low-level network programming (Java NIO) "from scratch" without reliance on heavy frameworks like Spring Boot or Netty.
*   **Business Domain:** Internet of Things (IoT), Networking, Educational (Software Architecture).

## 2. Core Technologies & Stack

*   **Languages:** Java 25.
*   **Build Tool:** Maven (Wrapper included).
*   **Frameworks & Runtimes:**
    *   **Runtime:** Java SE 25 (uses modern features like `var` and Records).
    *   **Frameworks:** None. The project explicitly avoids frameworks.
*   **Key Libraries/Dependencies:**
    *   **Lombok:** For boilerplate reduction (`@Slf4j`, `@RequiredArgsConstructor`, `var`).
    *   **SnakeYAML:** For parsing YAML configuration.
    *   **Jackson Databind:** For JSON processing (persistence).
    *   **SLF4J / Logback:** For logging.
*   **Package Manager:** Maven.

## 3. Architectural Patterns

*   **Overall Architecture:**
    *   **Event-Driven / Reactor-like:** Uses Java NIO (`Selector`, `SocketChannel`) for non-blocking I/O.
    *   **Layered:** Separates Networking (`ServerListener`), Protocol (`Decoder`/`Encoder`), Dispatching (`Handler`), and Domain Logic (`Authorization`, `TopicTree`).
*   **Design Patterns:** Explicitly implements specific patterns to solve architectural problems:
    *   **Strategy:** Authorization (File-based vs. Permissive).
    *   **Observer:** Event System (`BrokerEventPublisher`, `EventListener`).
    *   **Command:** Packet Dispatching (`MqttPacketHandler`).
    *   **Chain of Responsibility:** Packet Processing Pipeline (`Interceptor`).
    *   **Builder:** Object construction (`BrokerBuilder`, `PipelineBuilder`).
*   **Directory Structure Philosophy:**
    *   `src/main/java/com/mqtt/broker`: Root package.
    *   `/connection`: Low-level network handling (NIO).
    *   `/packet`: POJOs representing MQTT packets.
    *   `/handler`: Business logic for each packet type.
    *   `/interceptor`: Pipeline components for cross-cutting concerns.
    *   `/repository`: Data access (Authorization, Subscriptions).

## 4. Coding Conventions & Style Guide

*   **Formatting:**
    *   Indentation: 4 spaces.
    *   Standard Java brace placement (OTBS).
*   **Naming Conventions:**
    *   **Classes:** PascalCase (`MqttPacket`).
    *   **Variables/Methods:** camelCase (`handleRead`, `clientChannel`).
    *   **Constants:** UPPER_SNAKE_CASE (`KEEP_ALIVE_CHECK_INTERVAL_MS`).
*   **Modern Java Usage:**
    *   Extensive use of the `var` keyword for local variable type inference.
    *   Use of `Lombok` annotations to avoid getters/setters/constructors.
*   **Logging:**
    *   Use `@Slf4j` for logging.
    *   Log levels: `INFO` for lifecycle events, `ERROR` for exceptions, `WARN` for timeouts/unexpected states.

## 5. Key Files & Entrypoints

*   **Main Entrypoint:** `src/main/java/com/mqtt/broker/Main.java`.
*   **Configuration:**
    *   `config.yml`: Main server configuration (Port, Host, Auth settings).
    *   `users.json`: User credentials and permissions (when `allowAnonymous: false`).
    *   `sessions.json`: Persistent session storage.
*   **CI/CD Pipeline:** None currently detected.

## 6. Development & Testing Workflow

*   **Local Development Environment:**
    *   **Build & Run:** Use the Maven wrapper: `./mvnw clean compile exec:java`.
    *   **Prerequisites:** Java 25 installed.
*   **Testing:**
    *   **Current State:** The `src/test` directory is empty, and `pom.xml` does not list standard test dependencies (JUnit/Mockito).
    *   **Recommendation:** Any new features should ideally be accompanied by tests, but the infrastructure for this needs to be set up (add JUnit 5 dependencies).
*   **CI/CD Process:** Manual deployment/execution.

## 7. Specific Instructions for AI Collaboration

*   **Framework Avoidance:** Do NOT introduce frameworks like Spring, Netty, or Quarkus. The goal is "from scratch" implementation.
*   **Java Version:** Ensure all generated code is compatible with Java 25 (e.g., records, pattern matching, switch expressions are encouraged).
*   **Lombok:** Always use Lombok for data classes and logging.
*   **Pattern Adherence:** When adding new functionality, determine if it fits into an existing pattern (e.g., a new packet type should have a new `PacketHandler` and `Decoder`/`Encoder`).
