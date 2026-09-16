---
id: transactions
slug: /features/mongo/transactions
sidebar_position: 100
title: 事务支持
---

MongoDB 服务端支持事务，但当前 JDBC Mongo 适配器没有接入数据库事务。框架的事务配置不会自动补上这项能力。

例如第一篇文档插入成功、第二篇失败后，第一篇仍然保留。需要原生 MongoDB 事务时，须采用已经接入该能力的访问方式。

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

当前 JDBC 路径不支持设置事务隔离级别。MongoDB 的 readConcern、writeConcern 不是 JDBC 隔离级别，驱动不会把 Isolation 枚举自动转换成这些配置。

通用用法见[数据库事务](../../../guides/transaction/about.md)。差异表中的“有限”表示上述调用或事务效果受到限制，不表示框架缺少这些 API。
