---
id: params
sidebar_position: 4
title: 连接参数
description: jdbc-elastic 连接参数、别名、预读和自定义客户端。
---

## 连接参数

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| `server` | JDBC URL 的 host 部分。支持 `host:port` 或 `host1:port;host2:port`。 | 来自 URL |
| `user` / `username` | 认证用户名。 | 无 |
| `password` | 认证密码。 | 空 |
| `connectTimeout` | 连接超时（毫秒）。 | 驱动默认值 |
| `socketTimeout` | Socket 读取超时（毫秒）。 | 驱动默认值 |
| `timeZone` | 驱动用于类型转换的时区（例如 `+08:00`）。 | `UTC` |
| `indexRefresh` | 写入操作追加 `refresh=true`。 | `false` |
| `preRead` | 是否启用预读模式。 | `true` |
| `preReadThreshold` | 预读阈值，支持 `B/KB/MB/GB`。 | `5MB` |
| `preReadMaxFileSize` | 预读最大文件大小，支持 `B/KB/MB/GB`。 | `20MB` |
| `preReadCacheDir` | 预读缓存目录。 | `java.io.tmpdir` |
| `customElastic` | 实现 `CustomElastic` 的类全名。 | 无 |
| `clientName` | 已声明但未应用的参数。 | 未应用 |

`user` 是注册参数名，`username` 是兼容别名；两者同时设置时优先 user。`server`/`adapterName` 通常由 URL 提供。多主机用分号分隔，省略端口分别采用 9200；不支持将任意官方客户端 URI 参数直接透传。

## 预读与结果列

`preRead=true` 默认预读结果并展开文档字段，同时保留 `_ID`/`_DOC`。关闭时按专用列读取原始文档；不要继续假设存在展开字段。preReadThreshold/preReadMaxFileSize 支持 B/KB/MB/GB；无单位按 MB，缓存目录默认 java.io.tmpdir。预读可能增加内存、磁盘和首行等待成本，并不是服务端分页上限。

## 自定义客户端

`customElastic` 指定实现 `net.hasor.dbvisitor.adapter.elastic.CustomElastic` 的工厂类，方法为 `createElasticClient(String jdbcUrl, Map<String, String> props)`。工厂返回客户端由 JDBC 连接使用和关闭。高级客户端配置由工厂自行设置，不能套用 Milvus 的 TLS 参数。
