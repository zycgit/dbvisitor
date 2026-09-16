---
id: transactions
slug: /features/clickhouse/transactions
sidebar_position: 100
title: 事务支持
---

ClickHouse JDBC 的事务行为取决于驱动版本、连接配置和服务端能力。未提供真实事务的连接上，框架的事务 API 不能保证提交与回滚。

例如第一条 INSERT 成功、第二条失败后，不能依靠事务回滚撤销第一条。部分 JDBC 兼容模式允许事务方法调用，但在没有底层事务时只记录日志，见 [ClickHouse JDBC 兼容实现](https://github.com/ClickHouse/clickhouse-java/blob/v0.6.3/clickhouse-jdbc/src/main/java/com/clickhouse/jdbc/internal/JdbcTransaction.java)。

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

当前连接不提供可生效的事务隔离级别设置。JDBC 接受或返回某个级别，不足以证明查询具有对应的隔离效果；等待 mutation 完成也不是事务隔离。

通用用法见[数据库事务](../../guides/transaction/about.md)。差异表中的“有限”表示上述调用或事务效果受到限制，不表示框架缺少这些 API。
