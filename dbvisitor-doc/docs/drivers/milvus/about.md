---
id: about
sidebar_position: 1
title: 介绍
description: Milvus JDBC 驱动的接入、连接和使用。
---

`jdbc-milvus` 是可独立使用的 JDBC 驱动，使用 SQL 风格命令访问 Milvus，支持数据读写、向量搜索、集合管理和导入任务。可以直接使用 `Connection`、`PreparedStatement` 和 `ResultSet`，不要求使用 dbVisitor API。

## 主要能力

- 支持集合、索引、分区、别名及访问权限管理。
- 支持参数化读写、主键回传、分页读取及向量搜索。
- 支持 Hybrid Search、rerank、BM25 和 Import 任务；具体功能受服务端版本及配置约束。
- 支持 TLS、双向证书认证及 Zilliz Cloud 连接。

## 开始使用

1. [引入依赖](./dependencies.mdx)：Maven 或 Gradle 配置。
2. [建立连接](./connection.mdx)：JDBC URL、认证及连接示例。
3. [参数配置](./params.md)：参数名称、默认值和单位。
4. [执行命令](execution.mdx)
5. [参数绑定](parameters.mdx)
6. [读取结果](results.md)
7. [使用限制](./limitations.md)：JDBC 接口及数据源特有限制。

## 使用前须知

- 运行环境要求 Java 17 或更高版本。
- 命令必须使用驱动支持的语法，不会自动转换任意关系型 SQL。
- 不支持 JDBC Batch 和事务。使用连接池、ORM 或其他 JDBC 工具前，请核对[驱动适配器限制](../limited.md)。
- Milvus 服务端最低要求 2.6.2，部分功能要求更高版本，具体见[版本与支持范围](../../features/milvus/compatibility.md)。TLS/mTLS 和 Zilliz Cloud 接入见[安全连接示例](./connection.mdx#tls)。

[命令参考](../../features/milvus/about.md) · [dbVisitor API 用法](../../features/milvus/dbvisitor/usage.mdx)
