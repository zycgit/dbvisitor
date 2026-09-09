---
id: params
sidebar_position: 4
title: Connection Parameters
description: jdbc-mongo connection properties, aliases, pre-read and custom clients.
---

## Connection Parameters

| Parameter | Description | Default |
| --- | --- | --- |
| `server` | Host segment from the JDBC URL. Supports `host:port` or `host1:port;host2:port`. | From URL |
| `database` | Default database when URL path is empty. | None |
| `user` / `username` | Username for authentication. | None |
| `password` | Password for authentication. | Empty |
| `mechanism` | Authentication mechanism: `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509`. Empty means `createCredential`. | Empty |
| `clientName` | MongoDB application name. | `Mongo-JDBC-Client` |
| `socketTimeout` | Socket read timeout (ms). | Driver default |
| `socketSndBuffer` | Socket send buffer size (bytes). | Driver default |
| `socketRcvBuffer` | Socket receive buffer size (bytes). | Driver default |
| `retryWrites` | Enable retry writes. | Driver default |
| `retryReads` | Enable retry reads. | Driver default |
| `timeZone` | Driver time zone used for type conversion (for example `+08:00`). | `UTC` |
| `customMongo` | Fully qualified class name implementing `CustomMongo`. | None |
| `connectTimeout` | Declared parameter but not applied by this adapter. | Not applied |
| `preRead` | Enable pre-read mode. | `true` |
| `preReadThreshold` | Pre-read threshold size. Accepts `B/KB/MB/GB`. | `5MB` |
| `preReadMaxFileSize` | Maximum pre-read file size. Accepts `B/KB/MB/GB`. | `20MB` |
| `preReadCacheDir` | Cache directory for pre-read mode. | `java.io.tmpdir` |

`user` is the registered property; `username` is a compatibility alias. When both are supplied, user takes precedence. `server`/`adapterName` normally come from the URL. Separate hosts with semicolons; the default port is 27017. Arbitrary vendor-client URI options are not automatically passed through.

## Pre-Read and Result Columns

`preRead=true` reads ahead and expands document fields while retaining `_ID`/`_JSON`. With pre-read disabled, use the dedicated raw-document columns rather than assuming expanded fields exist. preReadThreshold/preReadMaxFileSize support B/KB/MB/GB and default to MB without a unit; the cache directory defaults to java.io.tmpdir. Pre-read may increase memory, disk use and time to first row; it is not a server-side pagination limit.

## Custom Client

`customMongo` names a factory implementing `net.hasor.dbvisitor.adapter.mongo.CustomMongo`, with method `createMongoClient(String jdbcUrl, Map<String, String> props)`. The JDBC connection uses and closes the returned client. The factory owns advanced client configuration; Milvus TLS properties do not apply here.

The URL database path takes precedence over database and is also used as the default authentication database. The db prefix requires a selected database. connectTimeout is declared but not applied by the current factory; configure it through a custom client when needed. Selecting X-509 authentication does not itself enable TLS.
