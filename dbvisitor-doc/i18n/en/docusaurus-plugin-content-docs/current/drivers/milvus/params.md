---
id: params
sidebar_position: 4
title: Parameter Configuration
description: Connection parameter names, accepted values, defaults and constraints.
---

<span id="properties" />
<span id="tls" />
<span id="custom-client" />

| Parameter | Accepted values / type | Default | Description and constraints |
| --- | --- | --- | --- |
| `database` | String | SDK default database | Default database; the URL path takes precedence. Changing catalog/schema to another database after connection is unsupported. |
| `token` | String | Unset | When nonblank, takes precedence over user/password. |
| `user`, `password` | String | Unset | Without token, both must be nonblank to be passed to the SDK. |
| `secure` | `true` / `false` | false; enabled by TLS options | true/false. Plaintext by default; certificate or serverName settings enable TLS automatically. Explicit false conflicts with TLS settings and fails; there is no plaintext fallback. |
| `caPemPath`, `serverPemPath` | String | Unset | PEM trust certificates, mutually exclusive. caPemPath accepts a private CA/bundle; serverPemPath accepts a server certificate or CA bundle for one-way TLS. Omit both to use default system trust. |
| `clientPemPath`, `clientKeyPath` | String | Unset | PEM client certificate chain and unencrypted private key (PKCS#8 recommended) for mutual TLS. Supply both together with caPemPath. |
| `serverName` | String | JDBC hostname | Expected TLS server name, defaulting to the JDBC host. An override must be a DNS name with caPemPath or serverPemPath (SDK 2.6.22 requirement). Both SDK and HTTPS verify this identity; verification is not disabled. |
| `consistencyLevel` | `Strong` / `Bounded` / `Session` / `Eventually` | SDK / collection default | Query consistency. QueryIterator uses collection-default consistency rather than this override; see [consistency controls](execution.mdx#execution-controls). |
| `connectTimeout` | Integer (milliseconds) | SDK default | SDK connection timeout in milliseconds; positive values override SDK defaults. Also used for Import HTTP connections. |
| `rpcDeadline` | Integer (milliseconds) | SDK default | Per-RPC deadline in milliseconds; positive values override SDK defaults. Also participates in Import HTTP timeouts. |
| `keepAliveTime` | Integer (milliseconds) | SDK default | Interval between keep-alive calls. Positive values override the SDK default. |
| `keepAliveTimeout` | Integer (milliseconds) | SDK default | Timeout waiting for a keep-alive response. Positive values override the SDK default. |
| `idleTimeout` | Integer (milliseconds) | SDK default | Connection idle timeout. Positive values override the SDK default. |
| `keepAliveWithoutCalls` | `true` / `false` | SDK default | Whether to send keep-alive without calls: true/false. |
| `maxRetry` | Integer | 3 | Default 3; maximum retries after the first failed write, 0 disables retries. Currently covers paged UPDATE partial upserts and DELETE writes. |
| `customMilvus` | Fully qualified class name | Unset | Factory implementing CustomMilvus; the JDBC connection closes the returned client. See [custom client setup](connection.mdx#custom-client). |
| `interceptor` | Fully qualified class name | Unset | Full InvocationHandler class name with an instantiable no-argument constructor. Receives V2 SDK methods and must return SDK responses or delegate the call. |
| `adapterName` | `milvus` | From JDBC URL | Adapter identifier supplied by the URL. |
| `server` | `host[:port]` | From JDBC URL; port 19530 | One endpoint per connection; IPv6 addresses are unsupported. |
| `timeZone` | Time-zone ID or offset, e.g. UTC, +08:00 | UTC | Common-driver property, not a Milvus server time-zone setting. |
