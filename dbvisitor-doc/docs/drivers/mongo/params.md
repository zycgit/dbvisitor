---
id: params
sidebar_position: 4
hide_table_of_contents: true
title: 连接参数
description: jdbc-mongo 支持的 JDBC 连接参数列表。
---

`jdbc-mongo` 支持在 JDBC URL 中配置多种参数，用于控制连接行为、认证方式、超时设置等。

```text title='JDBC URL 格式'
jdbc:dbvisitor:mongo://server:port?database=0&param1=value1&param2=value2
```

- 服务地址，如果没有指定端口号将会采用 `27017` 作为默认端口，格式为：`ip` 或 `ip:port`，集群模式为 `ip:port;ip:port` 或 `ip;ip;ip `


## 基本参数

| 参数名        | 描述                                                                 | 默认值                 |
|------------|--------------------------------------------------------------------|---------------------|
| username   | 数据库用户名                                                             | 无                   |
| password   | 数据库密码                                                              | 无                   |
| mechanism  | 认证机制，支持 `PLAIN`, `SCRAM-SHA-1`, `SCRAM-SHA-256`, `GSSAPI`, `X-509` | 无, 自动协商             |
| clientName | 客户端名称，显示在 MongoDB 服务器日志中                                           | `Mongo-JDBC-Client` |

## 网络与超时

| 参数名             | 描述                 | 默认值   |
|-----------------|--------------------|-------|
| connectTimeout  | 连接超时时间（毫秒）         | 驱动默认值 |
| socketTimeout   | Socket 读取超时时间（毫秒）  | 驱动默认值 |
| socketSndBuffer | Socket 发送缓冲区大小（字节） | 驱动默认值 |
| socketRcvBuffer | Socket 接收缓冲区大小（字节） | 驱动默认值 |

## 读写策略

| 参数名         | 描述       | 默认值    |
|-------------|----------|--------|
| retryWrites | 是否启用重试写入 | `true` |
| retryReads  | 是否启用重试读取 | `true` |

## 预读配置 (Pre-Read)

这些参数用于控制大文件或大量数据的预读行为，在开启预读功能后，查询结果集将会展示文档中所有字段，而不是仅展示 `_ID` 、`_JSON`字段。

| 参数名                | 描述                               | 默认值      |
|--------------------|----------------------------------|----------|
| preRead            | 是否启用预读，开启预读功能后，查询结果集将会展示文档中所有字段。 | `true`   |
| preReadThreshold   | 预读数据换入磁盘到阀值。                     | `5mb`    |
| preReadMaxFileSize | 预读数据最大文件大小（MB）                   | `20mb`   |
| preReadCacheDir    | 预读缓存目录                           | (系统缓存目录) |

- preReadMaxFileSize，参数可以跟随单位 `b`, `kb`, `mb`, `gb` 使用，例如：`10mb`。不携带单位时，默认以 MB 为单位。

## 其它参数

| 参数名         | 描述                                                                           | 默认值   |
|-------------|------------------------------------------------------------------------------|-------|
| timeZone    | 驱动在处理时区类型数据时使用的本地时区。                                                         | `UTC` |
| customMongo | 可以自定义 MongoDB 客户端的创建，需要实现 `net.hasor.dbvisitor.adapter.mongo.CustomMongo` 接口 | 无     |
