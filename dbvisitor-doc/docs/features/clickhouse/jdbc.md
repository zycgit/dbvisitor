---
id: jdbc
slug: /features/clickhouse/programmatic
sidebar_position: 10
title: 编程式 API
---

## 批量化 {#batch}

多条写入和命令错误处理可用。MergeTree 不强制主键唯一，重复 ID 不会产生主键冲突异常；不能靠批量插入是否报错来去重，见[重复记录处理](conflict.mdx)。

批量调用返回不代表异步修改已经完成；写后立即读取时，见[等待数据变更完成](write.mdx#wait-for-writes)。

## 多结果 {#multiple-results}

当前 JDBC 驱动不支持 `multipleExecute` 收集多条命令的结果，也不支持 `call` 使用的 CallableStatement。查询应通过 `queryForObject`、`queryForList` 等方法分别执行。

## 存储过程与函数 {#routines}

支持 SELECT 中的标量函数与 FROM 中的表函数。不支持存储过程调用、CallableStatement 回调和 OUT 参数记录返回。位置参数、命名参数及函数示例见[函数查询](functions.md#functions)。
