---
id: params
sidebar_position: 4
title: Parameter Configuration
description: Connection parameter names, accepted values, defaults and constraints.
---

<span id="properties" />
<span id="connection-parameters" />

| Parameter | Accepted values / type | Default | Description and constraints |
| --- | --- | --- | --- |
| `server` | `host[:port][;host[:port]...]` | From URL | Host segment from the JDBC URL. Supports `host:port` or `host1:port;host2:port`. |
| `database` | String | None | Default database when URL path is empty. The URL database path takes precedence and selects the default authentication database. |
| `user` / `username` | String | None | Username for authentication. user takes precedence over username. |
| `password` | String | Empty | Password for authentication. |
| `mechanism` | `PLAIN` / `SCRAM-SHA-1` / `SCRAM-SHA-256` / `GSSAPI` / `X-509` | Empty | Authentication mechanism: `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509`. Empty means `createCredential`. |
| `clientName` | String | `Mongo-JDBC-Client` | MongoDB application name. |
| `socketTimeout` | Integer (milliseconds) | Driver default | Socket read timeout (ms). |
| `socketSndBuffer` | Integer (bytes) | Driver default | Socket send buffer size (bytes). |
| `socketRcvBuffer` | Integer (bytes) | Driver default | Socket receive buffer size (bytes). |
| `retryWrites` | `true` / `false` | Driver default | Enable retry writes. |
| `retryReads` | `true` / `false` | Driver default | Enable retry reads. |
| `timeZone` | Time-zone ID or offset, e.g. UTC, +08:00 | `UTC` | Driver time zone used for type conversion (for example `+08:00`). |
| `customMongo` | Fully qualified class name | None | Fully qualified class name implementing `CustomMongo`. |
| `preRead` | `true` / `false` | `true` | Enable pre-read mode. Expands document fields while retaining raw-document columns. Disabled mode returns dedicated raw-document columns. |
| `preReadThreshold` | Size with B / KB / MB / GB suffix | `5MB` | Pre-read threshold size. Accepts `B/KB/MB/GB`. Defaults to MB when no unit is supplied. |
| `preReadMaxFileSize` | Size with B / KB / MB / GB suffix | `20MB` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. Defaults to MB when no unit is supplied. |
| `preReadCacheDir` | Directory path | `java.io.tmpdir` | Cache directory for pre-read mode. |
| `adapterName` | `mongo` | From JDBC URL | Adapter identifier supplied by the JDBC URL. |
