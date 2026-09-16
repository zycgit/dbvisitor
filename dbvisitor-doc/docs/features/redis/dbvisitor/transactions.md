---
id: transactions
slug: /features/redis/transactions
sidebar_position: 100
title: 事务支持
---

JDBC Redis 没有提供 JDBC 事务提交与回滚。可以使用框架的非事务调用方式，但不能把事务管理器配置视为多条命令的回滚保障。

例如第一条 SET 成功、第二条命令失败后，第一条 SET 的值不会自动恢复。

## 注解式事务 {#annotations}

事务注解用于声明调用边界；标注后不代表数据库具备回滚能力。不能只凭方法调用成功就认定事务生效。

## 模板事务 {#templates}

模板可以组织回调执行，但回调抛出异常时，已经成功的写入不保证撤销。

## 编程式事务 {#programmatic}

手动调用开始、提交、回滚仍依赖 JDBC 实现。驱动可能拒绝事务调用，或仅作兼容处理；不能把调用未报错当作回滚成功。

## 跨 API 事务 {#shared-transactions}

JdbcTemplate 和 Mapper 共用数据源，不会因此获得跨调用的原子性。需要使用同一事务上下文，也必须先有真实的数据库事务支持。

## 事务传播 {#propagation}

没有现存事务时，`SUPPORTS`、`NEVER` 可以执行命令。框架提供传播规则，但加入、挂起、恢复事务与实际的数据提交、回滚是两件事；`NESTED` 的回滚效果还依赖保存点。

## 隔离级别 {#isolation}

当前 JDBC 路径不支持设置事务隔离级别。Redis 命令自身的原子性，不等于通过 READ_COMMITTED、SERIALIZABLE 等选项建立了 JDBC 事务隔离。

通用用法见[数据库事务](../../../guides/transaction/about.md)。差异表中的“有限”表示上述调用或事务效果受到限制，不表示框架缺少这些 API。
