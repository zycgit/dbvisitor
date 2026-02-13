---
id: isolation
sidebar_position: 3
title: 10.3 隔离级别
description: dbVisitor 事务隔离级别详解。
---

# 隔离级别

不同的隔离级别均以下面这张表为例子：

```sql
mysql> select * from students;
+----+-------+
| id | name  |
+----+-------+
|  1 | Alice |
+----+-------+
```

## DEFAULT

默认事务隔离级别，具体使用的隔离级别由数据库驱动决定。
- 常量 `Isolation.DEFAULT`

## 脏读 (Read Uncommitted)

最低的隔离级别。事务 A 可以读到事务 B 更新后但**未提交**的数据。如果事务 B 回滚，事务 A 读到的就是脏数据。
- 常量 `Isolation.READ_UNCOMMITTED`

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | set isolation level read uncommitted | | |
| T2 | begin | begin | |
| T3 | update students set name='bob' where id=1 | | |
| T4 | | select * from students where id=1 | 读到 'bob'（脏读） |
| T5 | rollback | | |
| T6 | | select * from students where id=1 | 仍然读到 'Alice' |

## 不可重复读 (Read Committed)

事务 A 只能读取到其他事务**已提交**的数据。但在同一事务中，两次相同查询可能返回不同结果（因为期间有其他事务提交了修改）。
- 常量 `Isolation.READ_COMMITTED`

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | set isolation level read committed | set isolation level read committed | |
| T2 | begin | begin | |
| T3 | | select * from students where id=1 | 读到 'Alice' |
| T4 | update students set name='bob' where id=1 | | |
| T5 | | select * from students where id=1 | 仍读到 'Alice'（未提交不可见） |
| T6 | commit | | |
| T7 | | select * from students where id=1 | 读到 'bob'（不可重复读） |
| T8 | | commit | |

## 可重复读 (Repeatable Read)

在事务执行期间，相同的查询总是返回相同的结果，即使其他事务已提交了修改。
- 常量 `Isolation.REPEATABLE_READ`

:::info[幻读]
在 Repeatable Read 下，一个事务可能遇到**幻读（Phantom Read）**：
第一次查询某条记录发现不存在，但当更新这条记录时却能成功，再次查询它就出现了。
:::

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | set isolation level repeatable read | set isolation level repeatable read | |
| T2 | begin | begin | |
| T3 | | select * from students where id=99 | 结果为空 |
| T4 | insert into students (id, name) values (99, 'bob') | | |
| T5 | commit | | |
| T6 | | select * from students where id=99 | 仍为空（可重复读） |
| T7 | | update students set name='alice' where id=99 | 更新成功 |
| T8 | | select * from students where id=99 | 出现数据（幻读） |

## 同步事务 (Serializable)

最高的隔离级别。事务序列化执行，不能并发。脏读、不可重复读、幻读都不会出现。
- 常量 `Isolation.SERIALIZABLE`

:::caution[性能影响]
Serializable 相当于在开启事务时对整个数据库加 **排他锁**，直到事务提交后其他事务才能开始，效率会大大下降。
一般没有特别重要的场景不会使用此级别。
:::

## 各级别对比

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|---|:---:|:---:|:---:|
| READ_UNCOMMITTED | 可能 | 可能 | 可能 |
| READ_COMMITTED | - | 可能 | 可能 |
| REPEATABLE_READ | - | - | 可能 |
| SERIALIZABLE | - | - | - |
