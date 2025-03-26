---
id: propagation
sidebar_position: 2
title: 10.2 传播行为
description: 使用 dbVisitor ORM 的事务传播行为。
---

# 事务传播行为

简单的理解就是多个事务方法在同一个线程中相互调用时，事务如何在这些方法间传播。

## 加入已有事务 (REQUIRED)

- 原理：尝试加入已经存在的事务中，如果没有则开启一个新的事务。
- 常量 Propagation.REQUIRED

```text
| Time  | Transaction A   | Transaction B   | Effect            |
|-------|-----------------|-----------------|-------------------|
| 00:01 | begin           |                 | begin A           |
| 00:02 | insert data1    |                 |                   |
| 00:03 |                 | begin           | Do nothing        |
| 00:04 |                 | insert data2    |                   |
| 00:05 |                 | commit/rollback | Do nothing        |
| 00:06 | insert data3    |                 |                   |
| 00:07 | commit/rollback |                 | commit/rollback A |
```

## 独立事务 (REQUIRES_NEW)

- 原理：将挂起当前存在的事务挂起（如果存在的话）并且开启一个全新的事务，新事务与已存在的事务之间彼此没有关系。
- 常量 Propagation.REQUIRES_NEW

:::info
- 挂起会导致当前线程绑定的 Connection 在事务管理器中暂时无法获取。
- 在挂起之后事务管理器会创建一个新的 Connection 用作当前线程绑定的数据库连接。
:::

```text
| Time  | Transaction A   | Transaction B    | Effect                                 |
|-------|-----------------|------------------|----------------------------------------|
| 00:01 | begin           |                  | begin A                                |
| 00:02 | insert data1    |                  |                                        |
| 00:03 |                 | begin            | suspend A > new Connection B > begin B |
| 00:04 |                 | insert data2     |                                        |
| 00:05 |                 | commit/rollback  | commit/rollback B > resume A           |
| 00:06 | insert data3    |                  |                                        |
| 00:07 | commit/rollback |                  | commit/rollback A                      |
```

## 嵌套事务 (NESTED)

- 原理：在当前事务中通过 `Savepoint` 方式开启一个子事务。
- 常量 Propagation.NESTED

```text
| Time  | Transaction A   | Transaction B   | Effect                      |
|-------|-----------------|-----------------|-----------------------------|
| 00:01 | begin           |                 | begin A                     |
| 00:02 | insert data1    |                 |                             |
| 00:03 |                 | begin           | Savepoint B                 |
| 00:04 |                 | insert data2    |                             |
| 00:05 |                 | commit/rollback | commit/rollback Savepoint B |
| 00:06 | insert data3    |                 |                             |
| 00:07 | commit/rollback |                 | commit/rollback A           |
```

## 跟随环境 (SUPPORTS)

- 原理：如果当前没有事务存在，就以非事务方式执行；如果有，就使用当前事务。
- 常量 Propagation.SUPPORTS

:::info
SUPPORTS 行为是事务传播属性中最简单的一种行为，其行为本质上强调了 **不作为**
:::

```text
| Time  | Transaction A   | Transaction B   | Effect      |
|-------|-----------------|-----------------|-------------|
| 00:01 | begin           |                 | ...         |
| 00:02 | insert data1    |                 |             |
| 00:03 |                 | begin           | Do nothing  |
| 00:04 |                 | insert data2    |             |
| 00:05 |                 | commit/rollback | Do nothing  |
| 00:06 | insert data3    |                 |             |
| 00:07 | commit/rollback |                 | ...         |
```

- 若 Transaction A 开启了事务，那么效果等同于 REQUIRED

## 非事务方式 (NOT_SUPPORTED)

- 原理：如果当前没有事务存在，就以非事务方式执行；如果有，就将当前事务挂起。
- 常量 Propagation.NOT_SUPPORTED

```text
| Time  | Transaction A   | Transaction B       | Effect                  |
|-------|-----------------|---------------------|-------------------------|
| 00:01 | begin           |                     | ...                     |
| 00:02 | insert data1    |                     |                         |
| 00:03 |                 | begin               | if A exist > suspend A  |
| 00:04 |                 | insert data2        |                         |
| 00:05 |                 | commit/rollback     | if A suspend > resume A |
| 00:06 | insert data3    |                     |                         |
| 00:07 | commit/rollback |                     | ...                     |
```

## 排除事务 (NEVER)

- 原理：如果当前没有事务存在，就以非事务方式执行；如果有就抛出异常。
- 常量 Propagation.NEVER

```text
| Time  | Transaction A   | Transaction B   | Effect                       |
|-------|-----------------|-----------------|------------------------------|
| 00:01 | begin           |                 | ...                          |
| 00:02 | insert data1    |                 |                              |
| 00:03 |                 | begin           | if A exist > throw Exception |
| 00:04 |                 | insert data2    |                              |
| 00:05 |                 | commit/rollback | Do nothing                   |
| 00:06 | insert data3    |                 |                              |
| 00:07 | commit/rollback |                 | ...                          |
```

## 要求事务 (MANDATORY)

- 原理：如果当前没有事务存在，就抛出异常；如果有，就使用当前事务。
- 常量 Propagation.MANDATORY

```text
| Time  | Transaction A   | Transaction B   | Effect                           |
|-------|-----------------|-----------------|----------------------------------|
| 00:01 | begin           |                 | ...                              |
| 00:02 | insert data1    |                 |                                  |
| 00:03 |                 | begin           | if A not exist > throw Exception |
| 00:04 |                 | insert data2    |                                  |
| 00:05 |                 | commit/rollback | Do nothing                       |
| 00:06 | insert data3    |                 |                                  |
| 00:07 | commit/rollback |                 | ...                              |
```
