---
id: compatibility
sidebar_position: 21
title: 版本与支持范围
---

## 版本要求

| 组件 | 版本 |
| --- | --- |
| Java | 17+ |
| Milvus Java SDK | 2.6.22，V2 API |
| Milvus 服务端 | 2.6.x，最低 2.6.2 |

不支持 2.6.2 之前的版本，Milvus 3.0 不在支持范围内。

部分语句要求更高的服务端版本：向量可空要求 2.6.18+，TRUNCATE 要求 2.6.11+。具体条件见对应语句页。

## 支持范围

通过 SQL 提供实体读写、向量及混合查询、集合和索引管理、导入任务及管理操作。[各语句页](about.md)列出语法和对应 SDK 方法；Import 使用官方 REST API。

连接、认证、TLS 和 Zilliz Cloud 见[建立连接](../../drivers/milvus/connection.mdx)。JDBC 接口支持范围见[使用限制](../../drivers/milvus/limitations.md)。
