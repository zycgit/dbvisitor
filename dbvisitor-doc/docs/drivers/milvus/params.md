---
id: params
sidebar_position: 4
hide_table_of_contents: true
title: 连接参数
description: jdbc-milvus 支持的 JDBC 连接参数列表。
---

`jdbc-milvus` 支持在 JDBC URL 中配置多种参数，用于控制连接行为、认证方式、超时设置等。

```text title='JDBC URL 格式'
jdbc:dbvisitor:milvus://host:port/database?param1=value1&param2=value2
```

- 服务地址格式为 `host:port`，如果没有指定端口号将采用 `19530` 作为默认端口。
- 集群模式下多个主机用 `;` 分隔，格式为 `host1:port1[:healthPort1];host2:port2[:healthPort2]`。
- `database` 通过 URL Path 指定，默认为 `default`。

## 认证参数

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `token` | Token 认证，设置后优先于 user/password | 无 |
| `user` | 用户名，与 `password` 配合使用 | 无 |
| `password` | 密码，与 `user` 配合使用 | 无 |

## 网络与超时

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `connectTimeout` | 连接超时时间（毫秒） | 驱动默认值 |
| `keepAliveTime` | Keep-Alive 间隔时间（毫秒） | 驱动默认值 |
| `keepAliveTimeout` | Keep-Alive 超时时间（毫秒） | 驱动默认值 |
| `keepAliveWithoutCalls` | 无调用时是否保持活跃 | `false` |
| `idleTimeout` | 空闲连接超时时间（毫秒） | 驱动默认值 |
| `rpcDeadline` | RPC 截止时间（毫秒） | 驱动默认值 |

## 其它参数

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `consistencyLevel` | 查询的一致性级别，可选值：`Strong`、`Session`、`Bounded`、`Eventually`。设置后所有查询自动使用该级别 | 无（使用集合默认级别） |
| `interceptor` | 客户端拦截器，需实现 `java.lang.reflect.InvocationHandler` | 无 |
| `customMilvus` | 自定义 Milvus 客户端，需实现 `net.hasor.dbvisitor.adapter.milvus.CustomMilvus` | 无 |
