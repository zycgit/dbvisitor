---
id: params
sidebar_position: 4
title: 连接参数
description: jdbc-mongo 连接参数、别名、预读和自定义客户端。
---

## 连接参数

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| `server` | JDBC URL 的 host 部分。支持 `host:port` 或 `host1:port;host2:port`。 | 来自 URL |
| `database` | 当 URL 路径为空时使用的默认数据库。 | 无 |
| `user` / `username` | 认证用户名。 | 无 |
| `password` | 认证密码。 | 空 |
| `mechanism` | 认证机制：`PLAIN`、`SCRAM-SHA-1`、`SCRAM-SHA-256`、`GSSAPI`、`X-509`。为空时使用 `createCredential`。 | 空 |
| `clientName` | MongoDB 应用名。 | `Mongo-JDBC-Client` |
| `socketTimeout` | Socket 读取超时（毫秒）。 | 驱动默认值 |
| `socketSndBuffer` | Socket 发送缓冲区大小（字节）。 | 驱动默认值 |
| `socketRcvBuffer` | Socket 接收缓冲区大小（字节）。 | 驱动默认值 |
| `retryWrites` | 是否启用重试写入。 | 驱动默认值 |
| `retryReads` | 是否启用重试读取。 | 驱动默认值 |
| `timeZone` | 驱动用于类型转换的时区（例如 `+08:00`）。 | `UTC` |
| `customMongo` | 实现 `CustomMongo` 的类全名。 | 无 |
| `connectTimeout` | 已声明但适配器未应用的参数。 | 未应用 |
| `preRead` | 是否启用预读模式。 | `true` |
| `preReadThreshold` | 预读阈值，支持 `B/KB/MB/GB`。 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小，支持 `B/KB/MB/GB`。 | `20MB` |
| `preReadCacheDir` | 预读缓存目录。 | `java.io.tmpdir` |

`user` 是注册参数名，`username` 是兼容别名；两者同时设置时优先 user。`server`/`adapterName` 通常由 URL 提供。多主机用分号分隔，省略端口分别采用 27017；不支持将任意官方客户端 URI 参数直接透传。

## 预读与结果列

`preRead=true` 默认预读结果并展开文档字段，同时保留 `_ID`/`_JSON`。关闭时按专用列读取原始文档；不要继续假设存在展开字段。preReadThreshold/preReadMaxFileSize 支持 B/KB/MB/GB；无单位按 MB，缓存目录默认 java.io.tmpdir。预读可能增加内存、磁盘和首行等待成本，并不是服务端分页上限。

## 自定义客户端

`customMongo` 指定实现 `net.hasor.dbvisitor.adapter.mongo.CustomMongo` 的工厂类，方法为 `createMongoClient(String jdbcUrl, Map<String, String> props)`。工厂返回客户端由 JDBC 连接使用和关闭。高级客户端配置由工厂自行设置，不能套用 Milvus 的 TLS 参数。

URL 的数据库路径优先于 database 属性，亦用于默认认证库。未选数据库时不能使用 db 前缀。connectTimeout 虽已声明，但当前工厂没有应用它；需要该设置时通过自定义客户端配置。选择 X-509 认证机制本身不等于启用了 TLS。
