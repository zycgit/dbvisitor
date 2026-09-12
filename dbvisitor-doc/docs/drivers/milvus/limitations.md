---
id: limitations
sidebar_position: 4
title: 使用限制
---

## JDBC 接口

不支持 JDBC Batch、事务、保存点和可更新 ResultSet。多命令执行、单条命令的多行写入不等于 JDBC Batch。元数据接口仅提供部分信息；接入 ORM、连接池或迁移工具前，请核对[驱动适配器限制](../limited.md)。

## 数据源限制

不支持 JOIN、GROUP BY、列别名和标量 ORDER BY。分页 UPDATE/DELETE 不具备跨页原子性，失败时可能已有部分数据写入；应检查异常中的失败进度后再决定如何处理。服务端版本及向量功能要求见[版本与支持范围](../../features/milvus/compatibility.md)。

命令语法与返回值约定见[命令参考](../../features/milvus/about.md)。
