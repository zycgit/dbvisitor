---
id: limitations
sidebar_position: 4
title: 使用限制
---

## JDBC 接口

不支持 JDBC Batch、事务、保存点和可更新 ResultSet。多命令执行、单条命令的多行写入不等于 JDBC Batch。元数据接口仅提供部分信息；接入 ORM、连接池或迁移工具前，请核对[驱动适配器限制](../limited.md)。

## 数据源限制

请求使用 REST 风格命令，不支持将任意关系型 SQL 自动转换成 Elasticsearch 查询。默认预读会展开文档字段，关闭预读后应按 `_ID`、`_DOC` 等返回列读取。

命令语法与返回值约定见[命令参考](../../features/elastic/syntax/index.md)。

`clientName` 连接参数不生效；高级客户端配置通过 `customElastic` 提供。
