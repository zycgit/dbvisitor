---
id: propagation
sidebar_position: 2
title: 10.2 传播行为
description: dbVisitor 事务传播行为详解。
---

# 事务传播行为

当多个事务方法在同一线程中相互调用时，传播行为决定了事务如何在这些方法间传播。

## 加入已有事务 (REQUIRED)

尝试加入已经存在的事务中，如果没有则开启一个新的事务。
- 常量 `Propagation.REQUIRED`

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 加入事务 A（不做操作） |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 不做操作（由外层决定） |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 独立事务 (REQUIRES_NEW)

挂起当前已存在的事务（如果有），开启一个全新的独立事务，新事务与旧事务彼此无关。
- 常量 `Propagation.REQUIRES_NEW`

:::info
- 挂起会导致当前线程绑定的 Connection 暂时不可用。
- 挂起后事务管理器会创建一个新的 Connection 用作当前线程的数据库连接。
:::

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 挂起 A → 新建 Connection B → 开启事务 B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 提交/回滚 B → 恢复 A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 嵌套事务 (NESTED)

在当前事务中通过 `Savepoint` 方式开启一个子事务。子事务回滚不影响外层事务，但外层事务回滚会连带子事务。
- 常量 `Propagation.NESTED`

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 创建 Savepoint B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 释放/回滚 Savepoint B |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 跟随环境 (SUPPORTS)

如果当前没有事务，以非事务方式执行；如果有事务，则加入当前事务（效果等同 REQUIRED）。
- 常量 `Propagation.SUPPORTS`

:::info
SUPPORTS 的本质是**不作为** — 不会主动开启也不会阻止事务。
:::

## 非事务方式 (NOT_SUPPORTED)

如果当前没有事务，以非事务方式执行；如果有事务，则将当前事务**挂起**后以非事务方式执行。
- 常量 `Propagation.NOT_SUPPORTED`

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 挂起事务 A |
| T4 | | insert data2 | 非事务方式执行 |
| T5 | | commit/rollback | 恢复事务 A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 排除事务 (NEVER)

如果当前没有事务，以非事务方式执行；如果有事务，直接**抛出异常**。
- 常量 `Propagation.NEVER`

## 要求事务 (MANDATORY)

如果当前有事务，加入当前事务；如果没有事务，直接**抛出异常**。
- 常量 `Propagation.MANDATORY`

## 传播行为对比

| 传播行为 | 无事务时 | 有事务时 |
|---|---|---|
| `REQUIRED` | 新建事务 | 加入已有事务 |
| `REQUIRES_NEW` | 新建事务 | 挂起已有 → 新建事务 |
| `NESTED` | 新建事务 | Savepoint 子事务 |
| `SUPPORTS` | 非事务执行 | 加入已有事务 |
| `NOT_SUPPORTED` | 非事务执行 | 挂起已有 → 非事务执行 |
| `NEVER` | 非事务执行 | 抛出异常 |
| `MANDATORY` | 抛出异常 | 加入已有事务 |
