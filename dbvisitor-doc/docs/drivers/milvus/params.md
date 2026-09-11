---
id: params
sidebar_position: 4
title: 参数配置
description: 连接参数名称、可配置值、默认值及约束。
---

<span id="properties" />
<span id="tls" />
<span id="custom-client" />

| 参数名 | 可配置值 / 类型 | 默认值 | 作用与约束 |
| --- | --- | --- | --- |
| `database` | 字符串 | SDK 默认数据库 | 默认数据库；URL 路径优先。连接后不能切换到其他 catalog/schema。 |
| `token` | 字符串 | 未设置 | Token 认证，非空时优先于 user/password。 |
| `user`、`password` | 字符串 | 未设置 | 未设置 token 时，两者均非空才传递给 SDK。 |
| `secure` | `true` / `false` | false；TLS 配置可自动启用 | true/false。默认明文；配置证书或 serverName 时自动开启 TLS。显式 false 与 TLS 配置冲突时报错，不回退明文。 |
| `caPemPath`、`serverPemPath` | PEM 文件路径 | 系统信任配置 | PEM 信任证书，二选一。caPemPath 支持私有 CA/证书链；serverPemPath 支持服务端证书或 CA 链的单向认证。未设置则使用系统默认信任配置。 |
| `clientPemPath`、`clientKeyPath` | PEM 文件路径 | 未设置 | 双向 TLS 的 PEM 客户端证书链和无密码私钥（建议 PKCS#8）；必须成对设置，并提供 caPemPath。 |
| `serverName` | 字符串 | JDBC 主机名 | TLS 预期服务端名称，默认 JDBC 主机名；覆盖名称须为 DNS 名，并提供 caPemPath 或 serverPemPath（SDK 2.6.22 的路径要求）。SDK 与 HTTPS 均校验该名称，不关闭证书校验。 |
| `consistencyLevel` | `Strong` / `Bounded` / `Session` / `Eventually` | SDK / 集合默认 | 查询一致性。QueryIterator 使用集合默认一致性，不应用该覆盖值，详见[一致性控制](../../features/milvus/jdbc.mdx#execution-controls)。 |
| `connectTimeout` | 整数（毫秒） | SDK 默认 | SDK 连接超时，毫秒；正数覆盖 SDK 默认，也用于 Import HTTP 建连。 |
| `rpcDeadline` | 整数（毫秒） | SDK 默认 | 单次 RPC 截止时间，毫秒；正数覆盖 SDK 默认，也参与 Import HTTP 请求超时。 |
| `keepAliveTime` | 整数（毫秒） | SDK 默认 | keep-alive 发送间隔。正数覆盖 SDK 默认值。 |
| `keepAliveTimeout` | 整数（毫秒） | SDK 默认 | 等待 keep-alive 响应的超时时间。正数覆盖 SDK 默认值。 |
| `idleTimeout` | 整数（毫秒） | SDK 默认 | 连接空闲超时时间。正数覆盖 SDK 默认值。 |
| `keepAliveWithoutCalls` | `true` / `false` | SDK 默认 | 是否在无调用时发送 keep-alive，true/false。 |
| `maxRetry` | 整数 | 3 | 默认 3；初次写入失败后最多重试次数，0 禁用。适用于分页 UPDATE Partial Upsert 和 DELETE 写调用。 |
| `customMilvus` | 类全限定名 | 未设置 | 实现 CustomMilvus 的工厂；返回客户端由 JDBC 连接关闭。见[自定义客户端](./connection.mdx#custom-client)。 |
| `interceptor` | 类全限定名 | 未设置 | InvocationHandler 类全名，具有可实例化的无参构造器；接收 V2 SDK 方法，负责返回 SDK 响应或委托调用。 |
| `adapterName` | `milvus` | 来自 JDBC URL | 适配器标识，由 URL 提供。 |
| `server` | `host[:port]` | 来自 JDBC URL；端口 19530 | 每个连接使用一个服务地址，不支持 IPv6。 |
| `timeZone` | 时区 ID 或偏移量，如 UTC、+08:00 | UTC | 公共驱动属性，不是 Milvus 服务端时区设置。 |
