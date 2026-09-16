---
id: jdbc
slug: /features/db2/programmatic
sidebar_position: 10
title: 编程式 API
---

## 多结果 {#multiple-results}

`call` 可以收集存储过程通过 `WITH RETURN TO CLIENT` 游标返回的结果集。不能把多条 SELECT 用分号拼接后交给 `multipleExecute` 一次读取；独立查询应分别执行。

## 存储过程与函数 {#routines}

支持存储过程的 IN / OUT / INOUT 标量参数，以及标量函数和表函数查询。不支持通过 JDBC REF_CURSOR OUT 参数接收游标；返回记录时，使用过程直接返回的结果集。

参数配置及调用示例见[存储过程调用](procedures.md#parameters)。
