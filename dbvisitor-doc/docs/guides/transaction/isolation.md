---
id: isolation
sidebar_position: 3
title: 10.3 隔离级别
description: dbVisitor ORM 事务隔离级别的使用。
---

# 隔离级别

不同的隔离级别均已下面这张表为例子：

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

默认事务隔离级别，具体使用的数据库事务隔离级别由底层决定。
- 常量 Isolation.DEFAULT

:::info
相当于没有设置，但具体行为由驱动决定。
:::

## 脏读 (Read Uncommitted)

Read Uncommitted 是隔离级别最低的一种事务级别。在这种隔离级别下，一个事务会读到另一个事务更新后但未提交的数据，如果另一个事务回滚，那么当前事务读到的数据就是脏数据。
- 常量 Isolation.READ_UNCOMMITTED

```text
| Time  | Transaction A                                     | Transaction B                        | Effect                  |
|-------|---------------------------------------------------|--------------------------------------|-------------------------|
| 00:01 | set transaction isolation level read uncommitted; |                                      |                         |
| 00:02 | begin;                                            | begin;                               |                         |
| 00:03 | update students set name = 'bob' where id = 1;    |                                      |                         |
| 00:04 |                                                   | select * from students where id = 1; | see the change at 00:03 |
| 00:05 | update students set name = 'bob' where id = 2;    |                                      |                         |
| 00:06 | rollback;                                         |                                      |                         |
| 00:07 |                                                   | select * from students where id = 1; | see the change at 00:01 |
| 00:08 |                                                   | commit;                              |                         |
```

## 不可重复读 (Read Committed)

Read Committed 不可重复读，是指在数据库访问中，一个事务范围内两个相同的查询却返回了不同数据。
- 常量 Isolation.READ_COMMITTED

```text
| Time  | Transaction A                                   | Transaction B                                   | Effect                  |
|-------|-------------------------------------------------|-------------------------------------------------|-------------------------|
| 00:01 | set transaction isolation level read committed; | set transaction isolation level read committed; |                         |
| 00:02 | begin;                                          | begin;                                          |                         |
| 00:03 |                                                 | select * from students where id = 1;            | original data           |
| 00:04 | update students set name = 'bob' where id = 1;  |                                                 |                         |
| 00:05 |                                                 | select * from students where id = 1;            | original data           |
| 00:06 | commit;                                         |                                                 |                         |
| 00:07 |                                                 | select * from students where id = 1;            | see the change at 00:06 |
| 00:08 |                                                 | commit;                                         |                         |
```

## 可重复读取 (Repeatable Read)

可重复读(Repeatable Read)，当使用可重复读隔离级别时，在事务执行期间会锁定该事务以任何方式引用的所有行。
- 常量 Isolation.REPEATABLE_READ

:::info
在 Repeatable Read 隔离级别下，一个事务可能会遇到幻读（Phantom Read）的问题。

幻读是指，在一个事务中，第一次查询某条记录，发现没有。但是，当试图更新这条不存在的记录时，竟然能成功。并且，再次读取同一条记录，它就神奇地出现了。
:::

| Time  | Transaction A                                       | Transaction B                                     | Effect                         |
|-------|-----------------------------------------------------|---------------------------------------------------|--------------------------------|
| 00:01 | set transaction isolation level repeatable read;    | set transaction isolation level repeatable read;  |                                |
| 00:02 | begin;                                              | begin;                                            |                                |
| 00:03 |                                                     | select * from students where id = 99;             | result is empty                |
| 00:04 | insert into students (id, name) values (99, 'bob'); | select * from students where id = 99;             | see the change at 00:03        |
| 00:05 | commit;                                             |                                                   |                                |
| 00:06 |                                                     | update students set name = 'alice' where id = 99; | update is accepted             |
| 00:07 |                                                     | select * from students where id = 99;             | result is empty (Phantom Read) |
| 00:08 | rollback;                                           | commit;                                           |                                |

## 同步事务 (Serializable)

提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。因此，脏读、不可重复读、幻读都不会出现。
- 常量 Isolation.SERIALIZABLE

:::info
Serializable 隔离级别相当于在开启事务的时候，对整个数据库加了 **排他锁**，直到第一个事务被 commit 否则其它事务无法开始。

因此效率会大大下降，一般没有特别重要的情景，都不会使用 Serializable 隔离级别。
:::
