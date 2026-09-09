---
id: about
sidebar_position: 1
title: 简介
description: 使用 JDBC 和 SQL 子集访问 Milvus，涵盖参数化读写、向量检索、集合管理和导入任务。
---

jdbc-milvus 将 SQL 风格命令映射为 Milvus 官方 Java SDK 或 Import REST 调用，通过 `Connection`、`Statement`、`PreparedStatement`、`ResultSet` 访问。它采用 Apache 2.0 许可证，可独立使用，也可配合 dbVisitor 的 JdbcTemplate、Mapper 和构造器 API。

## 主要能力

- 集合、数据库、索引、分区、别名及用户/角色/权限管理。
- 参数化 INSERT/UPSERT、原生 Partial UPDATE、标量/KNN/范围 DELETE，以及分页写入的有界重试与失败进度。
- 标量、KNN、范围 SELECT 的按需分页；单查询向量的普通距离排序，以及服务端多路 Hybrid Search、RRF/Weighted rerank。
- Float/Binary/FP16/BF16/Sparse 向量、nullable/DEFAULT/Array schema 和 JDBC 主键回传。
- BM25/TextEmbedding schema 函数；多行写入、Import 提交及任务状态/失败观察。
- 单端口 SDK/REST 连接、TLS/双向证书、token/API key 和 Zilliz Cloud 标准端点配置。

上面是当前源码已接入的能力，实际可用性仍受服务端版本、索引、配置和权限约束。不是官方 SDK 全功能清单，也不是性能或云环境验收承诺。

## 版本与边界

当前源码 `6.7.1-SNAPSHOT`：Java 17+、Milvus Java SDK 2.6.22，服务端最低基线 Milvus 2.6.2。向量 nullable 要求 2.6.18+；2.5 及更早版本不在范围内，3.0 尚未建立支持承诺。发布版与源码版本的区别见[发布与支持矩阵](./compatibility.md)。

这是 SQL 子集，不是关系数据库模拟器。事务、JDBC batch、存储过程、可更新 ResultSet、JOIN/GROUP BY、列别名及标量 ORDER BY 不支持。接入 JDBC 框架时，应核对它实际使用的接口与 SQL，不能假设任意 ORM、BI 或迁移工具自动兼容。

## 从这里开始

- [安装与使用](./usecase.mdx)：依赖、完整可运行程序、分页、类型绑定、主键回传、Hybrid 和 Import。
- [连接参数与 TLS](./params.md)：全部连接属性、证书组合、Cloud 和自定义客户端。
- [语法手册](./commands.md)：精确语法、参数约束、命令返回列和 JDBC 多结果访问。
- [发布与支持矩阵](./compatibility.md)：版本、功能门槛、验证边界和发布核对。
- [dbVisitor API 用法](../../features/milvus/usage.mdx)：JdbcTemplate、构造器、BaseMapper、注解和 Mapper 文件。
