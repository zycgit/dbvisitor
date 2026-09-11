---
id: params
sidebar_position: 4
title: 参数配置
description: 连接参数名称、可配置值、默认值及约束。
---

<span id="properties" />
<span id="连接参数" />

| 参数名 | 可配置值 / 类型 | 默认值 | 作用与约束 |
| --- | --- | --- | --- |
| `server` | `host[:port][;host[:port]...]` | 来自 URL | JDBC URL 的 host 部分。支持 `host:port` 或 `host1:port;host2:port`。 |
| `database` | 字符串 | 无 | 当 URL 路径为空时使用的默认数据库。 URL 数据库路径优先，并用于默认认证库。 |
| `user` / `username` | 字符串 | 无 | 认证用户名。 同时设置时 user 优先于 username。 |
| `password` | 字符串 | 空 | 认证密码。 |
| `mechanism` | `PLAIN` / `SCRAM-SHA-1` / `SCRAM-SHA-256` / `GSSAPI` / `X-509` | 空 | 认证机制：`PLAIN`、`SCRAM-SHA-1`、`SCRAM-SHA-256`、`GSSAPI`、`X-509`。为空时使用 `createCredential`。 |
| `clientName` | 字符串 | `Mongo-JDBC-Client` | MongoDB 应用名。 |
| `socketTimeout` | 整数（毫秒） | 驱动默认值 | Socket 读取超时（毫秒）。 |
| `socketSndBuffer` | 整数（字节） | 驱动默认值 | Socket 发送缓冲区大小（字节）。 |
| `socketRcvBuffer` | 整数（字节） | 驱动默认值 | Socket 接收缓冲区大小（字节）。 |
| `retryWrites` | `true` / `false` | 驱动默认值 | 是否启用重试写入。 |
| `retryReads` | `true` / `false` | 驱动默认值 | 是否启用重试读取。 |
| `timeZone` | 时区 ID 或偏移量，如 UTC、+08:00 | `UTC` | 驱动用于类型转换的时区（例如 `+08:00`）。 |
| `customMongo` | 类全限定名 | 无 | 实现 `CustomMongo` 的类全名。 |
| `preRead` | `true` / `false` | `true` | 是否启用预读模式。 展开文档字段，同时保留原始文档列；关闭后使用专用原始文档列。 |
| `preReadThreshold` | 数值，可带 B / KB / MB / GB 单位 | `5MB` | 预读阈值，支持 `B/KB/MB/GB`。 无单位时按 MB。 |
| `preReadMaxFileSize` | 数值，可带 B / KB / MB / GB 单位 | `20MB` | 预读最大文件大小，支持 `B/KB/MB/GB`。 无单位时按 MB。 |
| `preReadCacheDir` | 目录路径 | `java.io.tmpdir` | 预读缓存目录。 |
| `adapterName` | `mongo` | 来自 JDBC URL | 适配器标识，由 JDBC URL 提供。 |
