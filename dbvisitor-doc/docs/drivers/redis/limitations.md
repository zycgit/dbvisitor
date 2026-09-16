---
id: limitations
sidebar_position: 4
title: 使用限制
---

## JDBC 接口

不支持 JDBC Batch、事务、保存点和可更新 ResultSet。多命令执行、单条命令的多行写入不等于 JDBC Batch。元数据接口仅提供部分信息；接入 ORM、连接池或迁移工具前，请核对[驱动适配器限制](../limited.md)。

## 数据源限制

Redis Cluster 连接不能通过 `database` 切换到非零数据库。默认客户端不启用 TLS；如需设置，使用 `customJedis` 自定义客户端。

命令语法与返回值约定见[命令参考](../../features/redis/about.md)。
