---
id: params
sidebar_position: 4
title: Connection Parameters and TLS
description: All jdbc-milvus properties, authentication, single-port TLS/mTLS and Zilliz Cloud connections.
---

See [Versions and Supported Scope](./compatibility.md) for dependency and server requirements.

URL format:

```text
jdbc:dbvisitor:milvus://host[:port][/database][?key=value&key2=value2]
```

Each JDBC connection uses one service address, with a default port of `19530`. SDK calls and Import REST share the URL host and port; no separate REST or management port is required. Cluster deployments can use a unified load-balancer endpoint. The URL database path overrides the `database` property. If both are omitted, the SDK default database applies. The current address parser does not support IPv6.

Supply parameters in the URL or `Properties`; URL values override same-name Properties values. Prefer Properties for credentials to avoid URL logging. The URL parser does not percent-decode values.

```java
Properties props = new Properties();
props.setProperty("user", "root");
props.setProperty("password", "YOUR_PASSWORD");
props.setProperty("consistencyLevel", "Strong");
// Or use props.setProperty("token", "YOUR_TOKEN"); token takes precedence.
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:milvus://127.0.0.1:19530/default", props)) {
    // Use JDBC here.
}
```

### Connection Parameters {#properties}

| Parameter | Actual behavior |
| --- | --- |
| `database` | Default database; the URL path takes precedence. Changing catalog/schema to another database after connection is unsupported. |
| `token` | When nonblank, takes precedence over user/password. |
| `user`, `password` | Without token, both must be nonblank to be passed to the SDK. |
| `secure` | true/false. Plaintext by default; certificate or serverName settings enable TLS automatically. Explicit false conflicts with TLS settings and fails; there is no plaintext fallback. |
| `caPemPath`, `serverPemPath` | PEM trust certificates, mutually exclusive. caPemPath accepts a private CA/bundle; serverPemPath accepts a server certificate or CA bundle for one-way TLS. Omit both to use default system trust. |
| `clientPemPath`, `clientKeyPath` | PEM client certificate chain and unencrypted private key (PKCS#8 recommended) for mutual TLS. Supply both together with caPemPath. |
| `serverName` | Expected TLS server name, defaulting to the JDBC host. An override must be a DNS name with caPemPath or serverPemPath (SDK 2.6.22 requirement). Both SDK and HTTPS verify this identity; verification is not disabled. |
| `consistencyLevel` | Consistency for SELECT/COUNT and paged DML selection: Strong, Bounded, Session, Eventually. When omitted, SDK/collection defaults apply. |
| `connectTimeout` | SDK connection timeout in milliseconds; positive values override SDK defaults. Also used for Import HTTP connections. |
| `rpcDeadline` | Per-RPC deadline in milliseconds; positive values override SDK defaults. Also participates in Import HTTP timeouts. |
| `keepAliveTime`, `keepAliveTimeout`, `idleTimeout` | SDK connection settings in milliseconds; positive values override SDK defaults. |
| `keepAliveWithoutCalls` | Whether to send keep-alive without calls: true/false. |
| `maxRetry` | Default 3; maximum retries after the first failed write, 0 disables retries. Currently covers paged UPDATE partial upserts and DELETE writes. |
| `customMilvus` | Full class name of a CustomMilvus factory; see below. |
| `interceptor` | Full InvocationHandler class name with an instantiable no-argument constructor. Receives V2 SDK methods and must return SDK responses or delegate the call. |
| `adapterName`, `server` | Normally populated from the URL, without separate configuration. |
| `timeZone` | Common-driver property, not a Milvus server time-zone setting. |

In Java code, `MilvusKeys` defines connection parameter names; `MilvusCommandKeys` defines SQL hints, command options and Import protocol fields. `timeout` is a SQL hint, not a connection-string parameter.

Distinguish these controls:

- `connectTimeout` / `rpcDeadline`: connection or per-RPC timeout, in milliseconds.
- `Statement.setQueryTimeout(seconds)`: this execution's time budget, including retries and later ResultSet page reads, in seconds.
- SQL hint `timeout`: synchronous IMPORT/LOAD/RELEASE wait, in milliseconds, default 60000. It is not a universal timeout for every command.
- `Statement.setFetchSize(rows)`: page size set before execution, not a JDBC URL property or a total row limit.

### TLS, Certificates and Zilliz Cloud {#tls}

For one-way TLS, provide a trusted CA, preferably through Properties. The JDBC URL format stays unchanged; the SDK uses gRPC TLS and Import uses HTTPS, on the same host and port:

```java
Properties props = new Properties();
props.setProperty("secure", "true");
props.setProperty("caPemPath", "/absolute/path/ca.crt");
props.setProperty("serverName", "localhost");
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:milvus://127.0.0.1:19530/default", props)) {
    // SDK and Import REST share TLS verification.
}
```

For mutual TLS, also set `clientPemPath=/absolute/path/client.crt` and `clientKeyPath=/absolute/path/client.key`. SDK and REST both use the port specified in the JDBC URL. Do not combine caPemPath/serverPemPath or use a server private key as client identity. A supplied trust file replaces rather than augments default system trust.

The deployment must expose the required protocols. Milvus 2.6.2 natively shares a plaintext gRPC/REST port but requires separate internal listeners with native TLS. For full Import support over TLS, expose a unified ingress. For example, Envoy can provide ALPN-based passthrough for gRPC (HTTP/2) and REST (HTTP/1.1), without terminating TLS or bypassing Milvus mutual authentication. The driver never probes another port, downgrades to plaintext, or resubmits a failed job through another API. A direct gRPC-only TLS endpoint supports SDK operations but not REST Import. See the [Milvus 2.6.2 listener implementation](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/distributed/proxy/listener_manager.go).

For Zilliz Cloud, use the public endpoint and token/API key from the console. Replace `https://` with the JDBC prefix, retain the endpoint host and port, and set `secure=true`. Use `443` when the HTTPS endpoint omits a port; preserve an explicitly supplied port such as `19530`. The JDBC default remains 19530; the driver does not infer Cloud type from a hostname:

```java
Properties props = new Properties();
props.setProperty("secure", "true");
props.setProperty("token", "YOUR_ZILLIZ_API_KEY");
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:milvus://YOUR_CLUSTER_HOST:443/default", props)) {
    // A publicly trusted certificate normally needs no PEM configuration.
}
```

There is no trust-all, hostname-verification bypass, or automatic plaintext fallback. Untrusted certificates, identity mismatch and missing mutual-TLS credentials fail. Keep credentials out of URLs and logs. Before connecting to Cloud, check endpoint permissions, network allowlists and Import API availability.

See the [Docker test environment](https://github.com/zycgit/dbvisitor/blob/main/dbvisitor-test/docker/README.md), [Milvus TLS configuration](https://milvus.io/docs/tls.md) and [Zilliz Cloud connection guide](https://docs.zilliz.com/docs/connect-to-cluster).

### Custom Clients {#custom-client}

A custom factory implements one method:

```java
public class MyMilvusFactory implements net.hasor.dbvisitor.adapter.milvus.CustomMilvus {
    @Override
    public io.milvus.v2.client.MilvusClientV2 createMilvusClient(
            String jdbcUrl, java.util.Map<String, String> props) {
        return new io.milvus.v2.client.MilvusClientV2(
                io.milvus.v2.client.ConnectConfig.builder()
                        .uri("http://127.0.0.1:19530").dbName("default").build());
    }
}
```

Register with `props.setProperty("customMilvus", MyMilvusFactory.class.getName())`. The factory owns SDK configuration. It is called once per JDBC connection; commands share its returned V2 client, which is closed with the connection. The driver limits SDK-internal retries to one attempt to avoid multiplying maxRetry. Use `conn.unwrap(MilvusClientV2.class)` to access the client, but do not close or reconfigure it while JDBC still uses it.

Import REST uses the JDBC address and authentication/TLS properties, not the custom SDK client's internal configuration; custom factories must keep these properties consistent. Standard Cloud connections use the cluster endpoint and do not require an exposed management port.
