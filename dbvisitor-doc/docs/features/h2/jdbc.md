---
id: jdbc
slug: /features/h2/programmatic
sidebar_position: 10
title: 编程式 API
---

## 多结果 {#multiple-results}

`call` 可以收集单条查询返回的结果集，但不能用 `multipleExecute` 一次收集分号分隔的多条 SELECT 结果。需要多个查询结果时，分别调用查询方法。

## 存储过程与函数 {#routines}

H2 通过 `CREATE ALIAS` 注册 Java 方法。返回标量时使用 `SELECT function(?)`，返回 ResultSet 时使用 `SELECT * FROM function(?)`，再由 JdbcTemplate 查询方法接收。

不支持通过 IN / OUT / INOUT 参数调用 SQL 存储过程。需要返回多个字段时，让函数返回 ResultSet，不使用存储过程的 OUT 参数配置。
