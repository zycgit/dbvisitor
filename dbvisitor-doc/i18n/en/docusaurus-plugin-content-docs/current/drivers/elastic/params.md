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
| `user` / `username` | String | None | Username for authentication. user takes precedence over username. |
| `password` | String | Empty | Password for authentication. |
| `connectTimeout` | Integer (milliseconds) | Driver default | Connection timeout (ms). |
| `socketTimeout` | Integer (milliseconds) | Driver default | Socket read timeout (ms). |
| `timeZone` | Time-zone ID or offset, e.g. UTC, +08:00 | `UTC` | Driver time zone used for type conversion (for example `+08:00`). |
| `indexRefresh` | `true` / `false` | `false` | Append `refresh=true` for write operations. |
| `preRead` | `true` / `false` | `true` | Enable pre-read mode. Expands document fields while retaining raw-document columns. Disabled mode returns dedicated raw-document columns. |
| `preReadThreshold` | Size with B / KB / MB / GB suffix | `5MB` | Pre-read threshold size. Accepts `B/KB/MB/GB`. Defaults to MB when no unit is supplied. |
| `preReadMaxFileSize` | Size with B / KB / MB / GB suffix | `20MB` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. Defaults to MB when no unit is supplied. |
| `preReadCacheDir` | Directory path | `java.io.tmpdir` | Cache directory for pre-read mode. |
| `customElastic` | Fully qualified class name | None | Fully qualified class name implementing `CustomElastic`. |
| `adapterName` | `elastic` | From JDBC URL | Adapter identifier supplied by the JDBC URL. |
