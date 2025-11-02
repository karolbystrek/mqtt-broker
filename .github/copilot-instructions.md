# Copilot Instructions for MQTT Broker Project

This document provides instructions for an AI coding agent to contribute effectively to this repository. Following these
guidelines will improve the quality of contributions and reduce the likelihood of pull request rejections.

## <Goals>

- **Reduce Rejections**: Ensure generated code passes all continuous integration (CI) builds and validation pipelines.
- **Minimize Failures**: Prevent common bash command and build failures by providing clear, validated steps.
- **Increase Efficiency**: Allow the agent to complete tasks faster by minimizing the need for repository exploration.

## <Limitations>

- These instructions are general and not task-specific.
- The total length should remain concise and easy to parse.

---

## <HighLevelDetails>

### Repository Summary

This repository contains a custom implementation of an **MQTT broker server**, written in **Java**.

The primary goals of the project are:

- To develop a server that supports the **MQTT protocol version 3.1.1 or 3.0**.
- The implementation specifically **excludes encryption/TLS**.
- The server must allow third-party clients to create and manage new topics.
- The system will feature a built-in mechanism for **pairing devices with user accounts**.

### Project & Technology Stack

- **Language**: Java (Version 17 or higher recommended)
- **Project Type**: Network Server (MQTT Broker)
- **Build Tool**: Apache Maven (Version 3.8.x or higher recommended)
- **Target Runtime**: Java Virtual Machine (JVM)

---

## Development Principles & Documentation

### Project Plan

All development work must follow the project plan outlined in the `PLAN.md` file. Before starting any task, consult this
file to identify the next unchecked step. Your contribution should focus on implementing that specific functionality.

- **Project Plan**:
  [PLAN.md](../PLAN.md)

### Code Quality & Architecture

You must adhere to the following principles to ensure the codebase is robust, maintainable, and scalable:

- **SOLID Principles**: Strictly follow SOLID principles in all new and refactored code.
- **Modularity**: Write modular and maintainable code. Prefer smaller, single-responsibility methods and helper objects
  over large, monolithic classes.
- **MQTT Specification**: All protocol-related implementations must adhere strictly to the official MQTT version 3.1.1
  documentation.

### Essential References

Use the following resources for reference and to ensure compliance with the project's goals and the MQTT protocol
standard:

- **Main Project Repository**:
  [https://github.com/karolbystrek/mqtt-broker](https://github.com/karolbystrek/mqtt-broker)
- **MQTT v3.1.1 Official Documentation**:
  [https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html)
- **MQTT Protocol Overview**: [https://stanford-clark.com/MQTT/](https://stanford-clark.com/MQTT/)

---

## <ProjectLayout>

### Architectural Overview

The project follows a standard Maven directory structure.

- `pom.xml`: The core project file. It defines dependencies, plugins, and build profiles. This is the first place to
  look for project configuration.
- `PLAN.md`: The development roadmap. **Always consult this file** for the next task.
- `src/main/java/`: Contains all the main application source code.
    - The main entry point for the server is likely located at
      `src/main/java/com/your-username/mqtt/broker/Server.java`.
- `README.md`: General information about the project.

---

## Final Instructions

**Trust these instructions.** The information provided here is validated and intended to be the single source of truth
for development procedures. Only perform a repository search if these instructions are incomplete, outdated, or found to
be in error. Do not try to compile the code or run tests unless explicitly instructed to do so. Prefer using stream API
rather than for loops when possible. Write code in a functional programming style. Use static imports of method and
constants wherever possible.
