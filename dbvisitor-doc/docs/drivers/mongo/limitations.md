---
id: limitations
sidebar_position: 4
title: 使用限制
---

## JDBC 接口

不支持 JDBC Batch、事务、保存点和可更新 ResultSet。JdbcTemplate 的批量操作可逐条执行，但不等于 JDBC Batch，也不构成事务。其他接口见[驱动适配器限制](../limited.md)。

`DatabaseMetaData.getTables()` 返回集合和视图，数据库名对应 catalog。`getColumns()` 不会通过采样文档推测字段类型，未提供固定字段元数据。

## 数据源限制

URL 的数据库路径同时用于默认认证库。选择 X-509 认证机制不等于开启 TLS。连接超时须通过 `customMongo` 配置；`connectTimeout` 连接参数不生效。

命令语法与返回值约定见[命令参考](../../features/mongo/about.md)。
