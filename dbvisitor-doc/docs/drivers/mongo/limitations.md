---
id: limitations
sidebar_position: 4
title: 使用限制
---

## JDBC 接口

不支持 JDBC Batch、事务、保存点和可更新 ResultSet。多命令执行、单条命令的多行写入不等于 JDBC Batch。元数据接口仅提供部分信息；接入 ORM、连接池或迁移工具前，请核对[驱动适配器限制](../limited.md)。

## 数据源限制

URL 的数据库路径同时用于默认认证库。选择 X-509 认证机制不等于开启 TLS。连接超时须通过 `customMongo` 配置；`connectTimeout` 连接参数不生效。

命令语法与返回值约定见[命令参考](../../features/mongo/syntax/index.md)。
