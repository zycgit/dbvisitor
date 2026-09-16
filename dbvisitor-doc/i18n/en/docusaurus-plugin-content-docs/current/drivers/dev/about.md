---
id: about
sidebar_position: 0
title: 1. Architecture
sidebar_label: 1. Architecture
description: Driver adapter layers, component responsibilities and execution flow.
---

A driver adapter exposes native database commands and SDK results through JDBC. Applications continue to use Connection, Statement and ResultSet; the adapter interprets commands and calls the target database.

## Layers

| Layer | Responsibility |
| --- | --- |
| Application access | Submit commands and read results through JDBC, JdbcTemplate, method annotations or Mapper files |
| Shared JDBC layer | Manage connection and statement state, bind parameters and expose standard result sets |
| Data source adapter | Parse native commands, assemble SDK requests and provide result cursors |
| Official SDK and database | Execute queries and writes and provide native database capabilities |

SQL, Redis commands and MongoDB commands are all command text at this boundary. The shared JDBC layer does not translate them into a universal SQL language or add database capabilities such as transactions or joins.

The Builder API and BaseMapper first need to generate commands. A dbVisitor database dialect handles generation, then passes the commands to the JDBC driver. **Dialects generate commands; adapters execute them.** A driver adapter can also be used independently of dbVisitor.

## Core Components

The core interfaces reside in `net.hasor.dbvisitor.driver`. The following components cooperate within a connection and its requests:

| Component | Responsibility | Lifetime |
| --- | --- | --- |
| `AdapterFactory` | Create connections and type support for an adapter name | Data source entry point registered through SPI |
| `AdapterConnection` | Own the underlying client; create, execute and cancel requests; release resources | One JDBC connection |
| `AdapterRequest` | Carry the command, bound parameters, timeout, fetch size and generated-key options | One execution request |
| `AdapterReceive` | Deliver result sets, update counts, generated keys or errors to JDBC | Receive execution results |
| `AdapterCursor` | Provide column information and rows | Read and close with the result set |

A request describes what to execute; the connection determines how to execute it. `AdapterReceive` delivers results, while `AdapterCursor` reads their data.

## Opening a Connection

1. The JDBC driver identifies the adapter name in the URL and merges connection parameters.
2. It locates the registered `AdapterFactory` and creates `TypeSupport` and `AdapterConnection`.
3. The factory initializes the SDK client and places it under the adapter connection's ownership.
4. The JDBC connection detects transaction and metadata capabilities for use through standard JDBC methods.

Closing the JDBC connection releases underlying resources through the adapter connection. Statements and requests reuse that connection instead of opening a new database connection for each command.

## Executing Commands and Reading Results

| Stage | Action |
| --- | --- |
| Create the request | Statement passes the command to `newRequest`, creating an `AdapterRequest` |
| Bind parameters | The JDBC layer places parameters and statement options into the request |
| Execute the command | `doRequest` parses the command and assembles and invokes SDK requests |
| Deliver results | `AdapterReceive` receives cursors, update counts or errors, followed by request completion |
| Read data | ResultSet reads rows from `AdapterCursor` and selects a converter when a target Java type is requested |
| Map objects | When using dbVisitor, mapping rules and TypeHandlers populate properties or objects |

The adapter chooses how to parse commands. Existing adapters use ANTLR, but the shared JDBC layer does not require it.

One execution may produce multiple JDBC results. A cursor can buffer a small result set or fetch SDK pages on demand. **Request completion does not mean all rows have been read.** Closing the result set must also release the cursor's fetching resources.

## Extension Capabilities

These interfaces extend the connection or type service rather than introducing another execution framework:

| Interface | Responsibility | Collaboration |
| --- | --- | --- |
| `TransactionSupport` | Auto-commit, isolation, commit and rollback | Provided by the connection; JDBC transaction methods delegate to it |
| `TypeSupport` | Describe adapter, JDBC and Java types and select converters | Provided by the factory for parameter identification and result access |
| `TypeConvert` | Convert one result value to a target Java type | Selected by TypeSupport, not registered separately on the connection |
| `MetadataSupport` | Query catalogs, schemas, tables, views and columns by path | The connection supplies native nodes; JDBC builds standard metadata result sets |

### Transaction Boundary

Provide `TransactionSupport` only when the underlying client can execute multiple commands in the same transaction. The shared JDBC layer forwards transaction operations; it does not simulate transactions. This interface does not include savepoints.

### Types and Conversion

`TypeSupport` describes a type and selects a converter; `TypeConvert` performs the conversion. These are driver-level services, separate from dbVisitor entity-field TypeHandlers. They do not automatically serialize SDK write parameters.

### Metadata Boundary

`MetadataSupport` returns objects and fields that actually exist in the database. The adapter queries native structures; the shared JDBC layer handles name-pattern filtering, sorting and standard result columns. It should not invent tables for Redis keys or infer field structures that the database does not declare.

## Implementing an Adapter

[Custom Driver](./guide) provides examples for creating a module, implementing connections and requests, returning results and registering the driver, followed by optional capability integration.
