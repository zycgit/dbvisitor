---
id: transactions
slug: /features/milvus/transactions
sidebar_position: 100
title: 事务支持
---

JDBC Milvus 没有提供数据库事务提交与回滚。事务 API 和传播配置不会让多条写入具备全部成功或全部回滚的效果。

例如分页 UPDATE 已完成前两页、第三页失败时，前两页不会回滚。失败进度及重试见 [UPDATE](../write/update.md)。

## 注解式事务 {#annotations}

事务注解用于声明调用边界；标注后不代表数据库具备回滚能力。不能只凭方法调用成功就认定事务生效。

## 模板事务 {#templates}

模板可以组织回调执行，但回调抛出异常时，已经成功的写入不保证撤销。

## 编程式事务 {#programmatic}

手动调用开始、提交、回滚仍依赖 JDBC 实现。驱动可能拒绝事务调用，或仅作兼容处理；不能把调用未报错当作回滚成功。

## 跨 API 事务 {#shared-transactions}

JdbcTemplate、Mapper 和构造器 API 共用数据源，不会因此获得跨调用的原子性。需要使用同一事务上下文，也必须先有真实的数据库事务支持。

## 事务传播 {#propagation}

没有现存事务时，`SUPPORTS`、`NEVER` 可以执行命令。框架提供传播规则，但加入、挂起、恢复事务与实际的数据提交、回滚是两件事；`NESTED` 的回滚效果还依赖保存点。

## 隔离级别 {#isolation}

当前 JDBC 路径不支持设置事务隔离级别。Milvus 的一致性级别控制查询可见性，不是 JDBC 隔离级别；设置 Strong 也不会提供多条写入的事务回滚。

通用用法见[数据库事务](../../../guides/transaction/about.md)。差异表中的“有限”表示上述调用或事务效果受到限制，不表示框架缺少这些 API。
