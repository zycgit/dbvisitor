---
sidebar_position: 4
title: 隔离级别
description: dbVisitor ORM 事务隔离级别的使用。
---

# 隔离级别

下面不同的隔离级别均已下面这张表为例子：

```sql
mysql> select * from students;
+----+-------+
| id | name  |
+----+-------+
|  1 | Alice |
+----+-------+
1 row in set (0.00 sec)
```

## DEFAULT

- dbVisitor 常量 `Isolation.DEFAULT`
- JDBC 常数 `Connection.TRANSACTION_NONE`

默认事务隔离级别，具体使用的数据库事务隔离级别由底层决定。

:::info
相当于没有设置，但具体行为由驱动决定。
:::

## 脏读 (Read Uncommitted)

- dbVisitor 常量 `Isolation.READ_UNCOMMITTED`
- JDBC 常数 `Connection.TRANSACTION_READ_UNCOMMITTED`

Read Uncommitted 是隔离级别最低的一种事务级别。在这种隔离级别下，一个事务会读到另一个事务更新后但未提交的数据，如果另一个事务回滚，那么当前事务读到的数据就是脏数据。

| 时间 | 事务1                                               | 事务2                                  | 效果              |
| ---- |---------------------------------------------------|--------------------------------------|-----------------|
| T1 | set transaction isolation level read uncommitted; |                                      |                 |
| T2 | begin;                                            | begin;                               |                 |
| T3 | update students set name = 'bob' where id = 1;    |                                      |                 |
| T4 |                                                   | select * from students where id = 1; | 可以看到 事务1 变更后的数据 |
| T5 | rollback;                                         |                                      |                 |
| T6 |                                                   | select * from students where id = 1; | 可以看到 事务1 回滚后的数据 |
| T7 |                                                   | commit;                              |                 |

## 不可重复读 (Read Committed)

- dbVisitor 常量 `Isolation.READ_COMMITTED`
- JDBC 常数 `Connection#TRANSACTION_READ_COMMITTED`

Read Committed 不可重复读，是指在数据库访问中，一个事务范围内两个相同的查询却返回了不同数据。

| 时间 | 事务1                                             | 事务2                                             | 效果                        |
|----|-------------------------------------------------|-------------------------------------------------|---------------------------|
| T1 | set transaction isolation level read committed; | set transaction isolation level read committed; |                           |
| T2 | begin;                                          | begin;                                          |                           |
| T3 |                                                 | select * from students where id = 1;            | 原始数据                      |
| T4 | update students set name = 'bob' where id = 1;  |                                                 |                           |
| T5 |                                                 | select * from students where id = 1;            | 此时 事务2 看到的仍然是 原始数据        |
| T6 | commit;                                         |                                                 |                           |
| T7 |                                                 | select * from students where id = 1;            | 此时 事务2 再次查询看到的数据和 原始数据 不一样 |
| T8 |                                                 | commit;                                         |                           |

## 可重复读取 (Repeatable Read)

- dbVisitor 常量 `Isolation.REPEATABLE_READ`
- JDBC 常数 `Connection#TRANSACTION_REPEATABLE_READ`

可重复读(Repeatable Read)，当使用可重复读隔离级别时，在事务执行期间会锁定该事务以任何方式引用的所有行。

在 Repeatable Read 隔离级别下，一个事务可能会遇到幻读（Phantom Read）的问题。
幻读是指，在一个事务中，第一次查询某条记录，发现没有。但是，当试图更新这条不存在的记录时，竟然能成功。并且，再次读取同一条记录，它就神奇地出现了。

| 时间 | 事务1                                                 | 事务2                                               | 效果     |
|----|-----------------------------------------------------|---------------------------------------------------|--------|
| T1 | set transaction isolation level repeatable read;    | set transaction isolation level repeatable read;  |        |
| T2 | begin;                                              | begin;                                            |        |
| T3 |                                                     | select * from students where id = 99;             | 不存在的数据 |
| T4 | insert into students (id, name) values (99, 'bob'); |                                                   |        |
| T5 | commit;                                             |                                                   |        |
| T6 |                                                     | update students set name = 'alice' where id = 99; | 可以成功更新 |
| T7 |                                                     | select * from students where id = 99;             |        |
| T8 |                                                     | commit;                                           |        |

- 在 T6 环节，由于 事务1 已经在 T5 环节递交。因此更新可以被成功执行，但是相对于 T3 环节成功更新了一个不存在的数据。这就是 **幻读**

## 同步事务 (Serializable)

- dbVisitor 常量 `Isolation.SERIALIZABLE`
- JDBC 常数 `Connection#TRANSACTION_SERIALIZABLE`

提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。因此，脏读、不可重复读、幻读都不会出现。

`Serializable` 隔离级别相当于在开启事务的时候，对整个数据库加了 `排他锁`，直到第一个事务被 commit 否则其它事务无法开始。
因此效率会大大下降，一般没有没有特别重要的情景，都不会使用 `Serializable` 隔离级别。
