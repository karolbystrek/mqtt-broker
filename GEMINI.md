# GEMINI.MD: AI Collaboration Guide

This document provides essential context for AI models interacting with this project. Adhering to these guidelines will ensure consistency and maintain code quality.

## 1. Project Overview & Purpose

* **Primary Goal:** Implementation of an MQTT 3.1.1 compliant broker. This project is developed for a "Design Patterns" university course, demonstrating manual implementation of networking protocols and software design patterns without heavy reliance on high-level frameworks like Spring Boot or Netty.
* **Business Domain:** IoT / Messaging / Network Protocols.

## 2. Core Technologies & Stack

* **Languages:** Java 25 (configured in `pom.xml`), though `README` mentions Java 21+.
* **Frameworks & Runtimes:**
    *   No major web/application framework (Pure Java).
    *   **Lombok** (v1.18.40) for boilerplate reduction.
* **Databases:**
    *   No external database system.
    *   File-based persistence/configuration (`users.json`, `sessions.json` in root) appears to be used.
* **Key Libraries/Dependencies:**
    *   **SnakeYAML:** Configuration parsing.
    *   **SLF4J & Logback:** Logging.
    *   **Jackson:** JSON processing (likely for the file-based persistence/config).
* **Package Manager:** Maven (Wrapper `mvnw` included).

## 3. Architectural Patterns

* **Overall Architecture:** Monolithic, Event-Driven.
    *   The application seems to follow a pipeline or reactor pattern for handling MQTT packets: `Decoder` -> `Handler` -> `Encoder`.
    *   Extensive use of Design Patterns (Strategy, Singleton, Observer/Listener, Factory inferred from naming).
* **Directory Structure Philosophy:**
    *   `src/main/java/com/mqtt/broker`: Root package.
    *   `/decoder` & `/encoder`: Protocol adaptation layer (parsing bytes to objects and vice versa).
    *   `/handler`: Business logic for processing specific MQTT packet types.
    *   `/packet`: POJOs representing MQTT packets (likely the DTOs).
    *   `/auth`: Authentication and Authorization logic, employing Strategy pattern.
    *   `/trie`: Topic tree data structure implementation (likely for subscription matching).
    *   `/event`: Event system for internal broker notifications.
    *   `/config`: Configuration loading logic.
    *   `/persistence`: Modules for saving session/state data.
    *   `/resources`: Contains `application.yml`.

## 4. Coding Conventions & Style Guide

* **Formatting:** Adhere to standard Java conventions.
    *   Indentation: 4 spaces (typical for Java).
* **Naming Conventions:**
    *   Classes: PascalCase (`ConnectPacketHandler`).
    *   Variables/Methods: camelCase (`handleConnect`).
    *   Constants: UPPER_SNAKE_CASE.
    *   Packages: all lowercase.
* **Modern Java Idioms (per `.github/copilot-instructions.md`):**
    *   **Java 25 Features:** Use latest features where appropriate.
    *   **`var`**: Use for local variables where types are obvious.
    *   **Records**: Use for data carriers/DTOs (e.g., Packet definitions) to ensure immutability.
    *   **Dependency Injection**: Use Constructor Injection. Avoid field injection. Use `@RequiredArgsConstructor` and `final` fields.
* **Error Handling:**
    *   Custom exceptions in `/exception` package (e.g., `InvalidPacketIdentifierException`).
    *   Use try-catch blocks in the main loop/entry point to prevent broker crash.

## 5. Key Files & Entrypoints

* **Main Entrypoint:** `src/main/java/com/mqtt/broker/Main.java`.
* **Configuration:**
    *   `src/main/resources/application.yml`: Main application config (host, port, logging).
    *   `users.json`, `sessions.json`: Runtime data/config storage.
* **Build Configuration:** `pom.xml`.

## 6. Development & Testing Workflow

* **Local Development Environment:**
    *   Requires Java 21+ (Java 25 recommended per pom).
    *   Build & Run: `./mvnw clean compile exec:java` (macOS/Linux) or `mvnw.cmd clean compile exec:java` (Windows).
* **Testing:**
    *   `src/test/java` exists but is currently empty.
    *   **Action for AI:** When adding features, *must* implement corresponding unit tests using JUnit/Mockito (implied standard, though dependencies might need to be added if not present in parent/implicit).
    *   Manual testing recommended via MQTTX client (as per README).

## 7. Specific Instructions for AI Collaboration

* **Design Patterns:** Since this is a Design Patterns course project, explicitly identify and strictly adhere to patterns (Strategy, Observer, etc.). When refactoring or adding features, prefer applying a formal design pattern over ad-hoc logic.
* **Dependencies:** Do not add heavy frameworks (Spring, Guice) without explicit user permission. Keep the core "pure Java".
* **Persistence:** Be careful with `users.json` and `sessions.json`. Ensure atomic writes or proper locking if modifying them to avoid corruption.
* **Java Version:** The project is configured for **Java 25** (`<source>25</source>`). Ensure generated code is compatible and utilizes modern syntax (e.g., pattern matching for switch, records).
