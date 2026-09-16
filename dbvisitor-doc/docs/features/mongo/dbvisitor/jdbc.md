---
id: jdbc
slug: /features/mongo/programmatic
sidebar_position: 10
title: 编程式 API
---

## 更新 {#updates}

`executeUpdate` 接收 `db.collection.insertOne(...)`、`updateMany(...)`、`deleteMany(...)` 等 MongoDB 命令，不写关系型 INSERT / UPDATE / DELETE。示例见[数据写入](write.mdx#exec-command)。

## 查询 {#queries}

通过 `find`、`aggregate` 等命令查询，再用 JdbcTemplate 读取实体、Map、单值或列表。字段来自结果文档，映射示例见[查询操作](query.mdx)。

## 批量化 {#batch}

多条命令可以通过 `executeBatch` 执行；重复 `_id` 和错误命令会报告异常。前面的写入可能已经成功，不会因后续错误自动撤销，见[多条写入与事务](write.mdx#transactions)。

## 存储过程与函数 {#routines}

可使用聚合表达式完成计算，再用查询方法接收结果。不支持 SQL 存储过程、CallableStatement 函数回调、OUT 参数记录和 SQL 表函数。示例见[聚合计算](query.mdx#functions)。
