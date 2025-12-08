---
id: params
sidebar_position: 4
title: 连接参数
description: jdbc-mongo 支持的 JDBC 连接参数列表。
---

`jdbc-mongo` 支持在 JDBC URL 中配置多种参数，用于控制连接行为、认证方式、超时设置等。

## 基本参数

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `username` | 数据库用户名 | 无 |
| `password` | 数据库密码 | 无 |
| `database` | 数据库名称 | 无 |
| `mechanism` | 认证机制，支持 `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509` | 自动协商 |
| `clientName` | 客户端名称，显示在 MongoDB 服务器日志中 | `Mongo-JDBC-Client` |

## 网络与超时

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `connectTimeout` | 连接超时时间（毫秒） | 驱动默认值 |
| `socketTimeout` | Socket 读取超时时间（毫秒） | 驱动默认值 |
| `socketSndBuffer` | Socket 发送缓冲区大小（字节） | 驱动默认值 |
| `socketRcvBuffer` | Socket 接收缓冲区大小（字节） | 驱动默认值 |

## 读写策略

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `retryWrites` | 是否启用重试写入 | `true` |
| `retryReads` | 是否启用重试读取 | `true` |

## 预读配置 (Pre-Read)

这些参数用于控制大文件或大量数据的预读行为。

| 参数名 | 描述 | 默认值 |
|---|---|---|
| `preRead` | 是否启用预读 | `false` |
| `preReadThreshold` | 预读阈值（MB），超过此大小触发预读 | - |
| `preReadMaxFileSize` | 预读最大文件大小（MB） | - |
| `preReadCacheDir` | 预读缓存目录 | - |

## 示例

```text
jdbc:mongo://127.0.0.1:27017/testdb?username=admin&password=123456&connectTimeout=5000&socketTimeout=3000
```
