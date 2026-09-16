---
id: jdbc
slug: /features/redis/programmatic
sidebar_position: 10
title: 编程式 API
---

## 更新 {#updates}

`executeUpdate` 执行 `SET`、`HSET`、`DEL` 等命令。`SET` 覆盖原值；`INCR` 等返回数据的命令使用查询方法接收，不能只根据命令是否修改数据选择 API。示例见[数据写入](write.mdx#exec-command)。

## 查询 {#queries}

查询内容是 Redis 命令，不是 SELECT。单值可用 `queryForObject("GET ?", key, String.class)`；Hash 字段映射为实体或 Map 时，按驱动的结果列配置，见[查询操作](query.mdx)。

## 批量化 {#batch}

`executeBatch` 可执行多次参数化命令，不等同于一条原子操作。错误命令会抛出异常，但 `SET` 已存在的键会覆盖原值，不会产生重复主键错误。需要一次写入多个键时可用 `MSET`，见[多键写入与事务](write.mdx#transactions)。

## 存储过程与函数 {#routines}

通过 `EVAL` 执行 Lua 计算，支持参数绑定和标量返回，也可使用 CallableStatement 回调。不支持 SQL 存储过程、OUT 参数记录和 SQL 表函数。脚本用法见[Lua 计算](query.mdx#functions)。
