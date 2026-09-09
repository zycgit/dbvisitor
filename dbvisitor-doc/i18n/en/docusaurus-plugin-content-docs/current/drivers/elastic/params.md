---
id: params
sidebar_position: 4
title: Connection Parameters
description: jdbc-elastic connection properties, aliases, pre-read and custom clients.
---

## Connection Parameters

| Parameter | Description | Default |
| --- | --- | --- |
| `server` | Host segment from the JDBC URL. Supports `host:port` or `host1:port;host2:port`. | From URL |
| `user` / `username` | Username for authentication. | None |
| `password` | Password for authentication. | Empty |
| `connectTimeout` | Connection timeout (ms). | Driver default |
| `socketTimeout` | Socket read timeout (ms). | Driver default |
| `timeZone` | Driver time zone used for type conversion (for example `+08:00`). | `UTC` |
| `indexRefresh` | Append `refresh=true` for write operations. | `false` |
| `preRead` | Enable pre-read mode. | `true` |
| `preReadThreshold` | Pre-read threshold size. Accepts `B/KB/MB/GB`. | `5MB` |
| `preReadMaxFileSize` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. | `20MB` |
| `preReadCacheDir` | Cache directory for pre-read mode. | `java.io.tmpdir` |
| `customElastic` | Fully qualified class name implementing `CustomElastic`. | None |
| `clientName` | Declared parameter but not applied by this adapter. | Not applied |

`user` is the registered property; `username` is a compatibility alias. When both are supplied, user takes precedence. `server`/`adapterName` normally come from the URL. Separate hosts with semicolons; the default port is 9200. Arbitrary vendor-client URI options are not automatically passed through.

## Pre-Read and Result Columns

`preRead=true` reads ahead and expands document fields while retaining `_ID`/`_DOC`. With pre-read disabled, use the dedicated raw-document columns rather than assuming expanded fields exist. preReadThreshold/preReadMaxFileSize support B/KB/MB/GB and default to MB without a unit; the cache directory defaults to java.io.tmpdir. Pre-read may increase memory, disk use and time to first row; it is not a server-side pagination limit.

## Custom Client

`customElastic` names a factory implementing `net.hasor.dbvisitor.adapter.elastic.CustomElastic`, with method `createElasticClient(String jdbcUrl, Map<String, String> props)`. The JDBC connection uses and closes the returned client. The factory owns advanced client configuration; Milvus TLS properties do not apply here.
