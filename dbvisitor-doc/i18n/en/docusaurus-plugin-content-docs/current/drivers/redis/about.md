---
id: about
sidebar_position: 0
title: Introduction
description: Redis JDBC capabilities, components and documentation.
---

jdbc-redis is a Redis JDBC driver adapter that allows developers to operate Redis data using standard JDBC interfaces and commands.
Its purpose is to enable developers to seamlessly use Redis through the familiar JDBC programming model.

## Core Features

- Provides core JDBC interfaces; frameworks must use the supported commands and calls.
- Supports **140+** commonly used commands, covering DB, Server, Keys, List, Set, SortedSet, String, Hash command sets.
- Supports command parameter placeholder `?`, with parameters set via `PreparedStatement`.
- Supports multi-command execution with results retrieved through standard JDBC methods.
- Supports `Statement` properties: `maxRows`, `fetchSize`, `setQueryTimeout(seconds)`.
- Supports command interceptors for logging, performance monitoring, and similar scenarios.
- Supports type conversion — for example, when result set returns `LONG` type, data can be retrieved via `ResultSet.getInt` or `ResultSet.getString`.
- Supports `BLOB`, `CLOB`, `NCLOB` reading.

## Technical

### Architecture

The jdbc-redis project uses the Adapter pattern to map standard JDBC interfaces to the Redis command model. Main components include:
- JedisConn: bridges the shared JDBC layer and Redis adapter, handling parsing and connection state. The public JDBC Connection is supplied by the shared driver.
- JedisCmd: wraps Jedis client command interfaces, supporting standalone and cluster modes.
- JedisRequest: represents a Redis command request.
- ANTLR4 parser: used to parse Redis commands and generate execution plans.

### Command Execution

- Users create Connection and Statement via the JDBC API and execute Redis commands.
- JedisConn receives the Redis command and parses it using the ANTLR4 parser.
- Parsed commands are forwarded by JedisCmd to the underlying Jedis client for execution.
- Results are returned to users via standard ResultSet or update counts.

### Dependencies

- Jedis: the Java client used by this adapter, version 6.1.0.
- ANTLR4: powerful parser generator used to parse Redis commands.
- dbVisitor-driver: the base database driver framework.

## Use Cases

- Accessing Redis in a unified way (JDBC) within Java projects.
- Operating Redis using native command syntax.
- Integrating Redis into existing JDBC-based data processing pipelines.

## Versions and Documentation

The current development version requires Java 17+ and depends on Jedis 6.1.0, ANTLR4 and dbvisitor-driver. The client dependency does not imply support for all new server commands; see this driver's command reference.

[Install and Use](./usecase.mdx) · [Connection Parameters](./params.md) · [149 commands and result columns](./commands.md) · [dbVisitor API](../../features/redis/usage.mdx)
