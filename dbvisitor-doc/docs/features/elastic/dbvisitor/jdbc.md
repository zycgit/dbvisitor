---
id: jdbc
slug: /features/elastic/programmatic
sidebar_position: 10
title: 编程式 API
---

## 更新 {#updates}

`executeUpdate` 接收 HTTP 方法、路径和请求体组成的命令，不写关系型 INSERT / UPDATE / DELETE。文档 ID、更新条件及示例见[数据写入](write.mdx#exec-command)。

## 查询 {#queries}

通过 `_search` 和 Query DSL 查询，JdbcTemplate 从驱动提供的结果列读取实体、Map、单值或列表。预读配置会影响字段展开，见[查询操作](query.mdx)。

## 存储过程与函数 {#routines}

可在 `_search` 的 `script_fields` 中执行计算并读取结果。不支持 SQL 存储过程、CallableStatement 函数回调、OUT 参数记录和 SQL 表函数。示例见[脚本计算](query.mdx#functions)。
