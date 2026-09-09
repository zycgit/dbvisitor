---
id: params
sidebar_position: 4
title: 连接参数与 TLS
description: jdbc-milvus 的全部连接参数、认证、单端口 TLS、双向证书和 Zilliz Cloud 连接配置。
---

本页对应当前源码版本，依赖及服务端支持范围见[发布与支持矩阵](./compatibility.md)。

URL 格式：

```text
jdbc:dbvisitor:milvus://host[:port][/database][?key=value&key2=value2]
```

每个 JDBC 连接使用一个服务地址，默认端口 `19530`；SDK 与 Import REST 共用 URL 的主机与端口，不需要单独指定 REST 或管理端口。集群部署可填写统一的负载均衡入口。URL 的数据库路径优先于 `database` 属性；省略二者时沿用 SDK 默认数据库。当前地址解析不支持 IPv6。

参数可放在 URL 或 `Properties` 中；同名 URL 参数覆盖 Properties。认证信息建议放入 Properties，避免 URL 日志泄露；当前 URL 解析不做百分号解码。

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

### 连接参数 {#properties}

| 参数 | 实际作用 |
| --- | --- |
| `database` | 默认数据库；URL 路径优先。连接后不能切换到其他 catalog/schema。 |
| `token` | Token 认证，非空时优先于 user/password。 |
| `user`、`password` | 未设置 token 时，两者均非空才传递给 SDK。 |
| `secure` | true/false。默认明文；配置证书或 serverName 时自动开启 TLS。显式 false 与 TLS 配置冲突时报错，不回退明文。 |
| `caPemPath`、`serverPemPath` | PEM 信任证书，二选一。caPemPath 支持私有 CA/证书链；serverPemPath 支持服务端证书或 CA 链的单向认证。未设置则使用系统默认信任配置。 |
| `clientPemPath`、`clientKeyPath` | 双向 TLS 的 PEM 客户端证书链和无密码私钥（建议 PKCS#8）；必须成对设置，并提供 caPemPath。 |
| `serverName` | TLS 预期服务端名称，默认 JDBC 主机名；覆盖名称须为 DNS 名，并提供 caPemPath 或 serverPemPath（SDK 2.6.22 的路径要求）。SDK 与 HTTPS 均校验该名称，不关闭证书校验。 |
| `consistencyLevel` | SELECT/COUNT 及分页 DML 选取的一致性：Strong、Bounded、Session、Eventually。不设置则沿用 SDK/集合默认。 |
| `connectTimeout` | SDK 连接超时，毫秒；正数覆盖 SDK 默认，也用于 Import HTTP 建连。 |
| `rpcDeadline` | 单次 RPC 截止时间，毫秒；正数覆盖 SDK 默认，也参与 Import HTTP 请求超时。 |
| `keepAliveTime`、`keepAliveTimeout`、`idleTimeout` | SDK 连接配置，毫秒；正数覆盖 SDK 默认。 |
| `keepAliveWithoutCalls` | 是否在无调用时发送 keep-alive，true/false。 |
| `maxRetry` | 默认 3；初次写入失败后最多重试次数，0 禁用。当前覆盖分页 UPDATE Partial Upsert 和 DELETE 写调用。 |
| `customMilvus` | 实现 CustomMilvus 的工厂类全名，见下节。 |
| `interceptor` | InvocationHandler 类全名，具有可实例化的无参构造器；接收 V2 SDK 方法，负责返回 SDK 响应或委托调用。 |
| `adapterName`、`server` | 通常由 URL 设置，无需单独传入。 |
| `timeZone` | 公共驱动属性，不是 Milvus 服务端时区设置。 |

Java 代码中的连接参数名由 `MilvusKeys` 定义；SQL Hint、命令选项及 Import 协议字段由 `MilvusCommandKeys` 定义。`timeout` 是 SQL Hint，不是连接字符串参数。

不要混淆四个控制项：

- `connectTimeout` / `rpcDeadline`：连接或单次 RPC 超时，毫秒。
- `Statement.setQueryTimeout(seconds)`：本次执行的时间预算，涵盖重试及后续 ResultSet 取页，单位秒。
- SQL Hint `timeout`：IMPORT/LOAD/RELEASE 同步等待，毫秒，默认 60000；不是所有命令的通用超时。
- `Statement.setFetchSize(rows)`：执行前设置单页大小；不是 JDBC URL 参数，也不是返回总量上限。

### TLS、证书与 Zilliz Cloud {#tls}

单向 TLS 使用可信 CA，证书路径建议放在 Properties 中。保留原 JDBC URL 格式；SDK 使用 gRPC TLS，Import 使用 HTTPS，两者访问同一主机和端口：

```java
Properties props = new Properties();
props.setProperty("secure", "true");
props.setProperty("caPemPath", "/absolute/path/ca.crt");
props.setProperty("serverName", "localhost");
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:milvus://127.0.0.1:2954/default", props)) {
    // SDK and Import REST share TLS verification.
}
```

双向 TLS 再设置 `clientPemPath=/absolute/path/client.crt` 和 `clientKeyPath=/absolute/path/client.key`；本仓库双向测试入口为 `2955`，SDK 和 REST 均使用该端口。不要同时设置 caPemPath/serverPemPath；不要把服务端私钥当作客户端私钥。指定信任文件后仅信任其中的证书，不自动追加系统根证书。

服务端部署须提供对应协议：Milvus 2.6.2 明文支持 gRPC/REST 原生共用端口，但原生 TLS 模式要求内部独立监听。若 TLS 下需要完整 Import 能力，应提供统一入口；本仓库使用 Envoy 按 ALPN 透传分流 gRPC（HTTP/2）和 REST（HTTP/1.1），不终止 TLS，也不绕过 Milvus 的双向认证。驱动不探测第二个端口，不降级明文，不在提交失败后改用其他接口重复提交。直接连接仅有 gRPC 的 TLS 端口可以使用 SDK 操作，但不能使用 REST Import。此限制来自 [Milvus 2.6.2 监听实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/distributed/proxy/listener_manager.go)。

Zilliz Cloud 使用控制台给出的公共端点和 token/API key。把端点的 `https://` 换为 JDBC 前缀，保留其主机和端口，并设置 `secure=true`。无端口的 HTTPS 端点使用 `443`；若端点明确提供 `19530` 等端口则保留。驱动默认端口仍为 19530，不根据域名猜测 Cloud 类型：

```java
Properties props = new Properties();
props.setProperty("secure", "true");
props.setProperty("token", "YOUR_ZILLIZ_API_KEY");
try (Connection conn = DriverManager.getConnection(
        "jdbc:dbvisitor:milvus://YOUR_CLUSTER_HOST:443/default", props)) {
    // A publicly trusted certificate normally needs no PEM configuration.
}
```

不提供 trust-all、跳过主机名校验或自动明文回退。证书不受信任、名称不匹配、缺少双向认证证书会失败。认证信息不要放进 URL 或日志。Cloud 的端点权限、网络白名单和 Import 可用性以实际集群为准；本地 TLS 成功不等于云端已验收。

本地环境见 [Docker 测试环境](https://github.com/zycgit/dbvisitor/blob/main/dbvisitor-test/docker/README.md)。依据：[Milvus TLS 配置](https://milvus.io/docs/tls.md)、[Zilliz Cloud 连接说明](https://docs.zilliz.com/docs/connect-to-cluster)。

### 自定义客户端 {#custom-client}

自定义工厂只实现一个方法：

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

用 `props.setProperty("customMilvus", MyMilvusFactory.class.getName())` 注册。工厂负责 SDK 配置；每个 JDBC 连接调用工厂一次，所有命令共享返回的 V2 客户端，连接关闭时释放。驱动将 SDK 内部重试设为单次尝试，避免与 maxRetry 叠加。可通过 `conn.unwrap(MilvusClientV2.class)` 获取客户端，但不要在 JDBC 连接仍使用它时手动关闭或重配。

Import REST 使用 JDBC 地址和认证/TLS 属性，不读取自定义 SDK 客户端的内部配置；自定义工厂也应同步配置这些属性。Cloud 标准连接使用集群端点，不要求公开管理端口。
