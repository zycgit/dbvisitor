---
id: isolation
sidebar_position: 6
title: 10.6 隔离级别
description: dbVisitor 事务隔离级别详解。
---

<span id="隔离级别" />

# 10.6 隔离级别

隔离级别决定多个事务并发读写同一批数据时，能看到什么结果。dbVisitor 的 `Isolation` 最终会映射到 JDBC `Connection#setTransactionIsolation(int)`。

隔离级别可以用于三种事务入口：

```java title='注解式事务'
@Transactional(isolation = Isolation.READ_COMMITTED)
public void createOrder(long orderId) {
    ...
}
```

```java title='模板事务'
txTemplate.execute(tranStatus -> {
    ...
    return null;
}, Propagation.REQUIRED, Isolation.REPEATABLE_READ);
```

```java title='编程式事务'
TransactionStatus tran = txManager.begin(
        Propagation.REQUIRED,
        Isolation.SERIALIZABLE
);
```

## 如何选择

| 想达到的效果 | 常用隔离级别 |
|---|---|
| 使用数据库默认配置 | `DEFAULT` |
| 避免读到未提交数据，兼顾并发性能 | `READ_COMMITTED` |
| 同一事务内多次读取同一行结果保持一致 | `REPEATABLE_READ` |
| 要求最高一致性，接受明显并发性能损耗 | `SERIALIZABLE` |
| 明确允许脏读，只追求很低一致性成本 | `READ_UNCOMMITTED` |

:::info
不同数据库对默认隔离级别和具体语义的实现可能不同。例如 MySQL、PostgreSQL、Oracle 对 `REPEATABLE_READ`、幻读、锁的处理并不完全一样。dbVisitor 负责设置 JDBC 隔离级别，但不能改变数据库自身的事务模型。
:::

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

```java title='使用数据库默认隔离级别'
@Transactional
public void createOrder(long orderId) {
    ...
}
```

## 脏读 (Read Uncommitted)

最低的隔离级别。事务 A 可以读到事务 B 更新后但**未提交**的数据。如果事务 B 回滚，事务 A 读到的就是脏数据。
- 常量 `Isolation.READ_UNCOMMITTED`

```java title='显式允许 READ_UNCOMMITTED'
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public List<OrderInfo> queryFastButWeakConsistent() {
    return orderMapper.queryRecentOrders();
}
```

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | | set isolation level read uncommitted | |
| T2 | begin | begin | |
| T3 | update students set name='bob' where id=1 | | |
| T4 | | select * from students where id=1 | 读到 'bob'（脏读） |
| T5 | rollback | | |
| T6 | | select * from students where id=1 | 读回 'Alice' |

## 不可重复读 (Read Committed)

事务 A 只能读取到其他事务**已提交**的数据。但在同一事务中，两次相同查询可能返回不同结果（因为期间有其他事务提交了修改）。
- 常量 `Isolation.READ_COMMITTED`

```java title='常见业务写入使用 READ_COMMITTED'
@Transactional(isolation = Isolation.READ_COMMITTED)
public void payOrder(long orderId) {
    orderMapper.markPaid(orderId);
    orderMapper.insertPayLog(orderId);
}
```

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

防止同一事务重复读取已有行时受到其他事务已提交修改的影响。自身写入、范围内新增行和锁定读的行为还取决于数据库实现。
- 常量 `Isolation.REPEATABLE_READ`

```java title='同一事务内需要稳定读取'
@Transactional(isolation = Isolation.REPEATABLE_READ)
public OrderSummary buildOrderSummary(long orderId) {
    Order order = orderMapper.queryOrder(orderId);
    List<OrderItem> items = orderMapper.queryItems(orderId);
    return new OrderSummary(order, items);
}
```

:::info[幻读]
在 Repeatable Read 下，一个事务可能遇到**幻读（Phantom Read）**：
幻读通常指同一范围查询因其他事务提交而出现或消失记录。下面展示 MySQL InnoDB 中一致性读与更新操作可能观察到不同数据的情形，不是所有数据库在该级别的共同表现。
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

## 可串行化 (Serializable)

要求已提交事务的效果等价于按某种顺序串行执行，不意味着事务必须逐个运行。它排除脏读、不可重复读、幻读及不可串行化结果。
- 常量 `Isolation.SERIALIZABLE`

```java title='需要最高一致性时使用'
@Transactional(isolation = Isolation.SERIALIZABLE)
public void allocateUniqueNumber(String bizType) {
    Long nextNumber = numberMapper.queryNextNumber(bizType);
    numberMapper.updateNextNumber(bizType, nextNumber + 1);
}
```

:::caution[性能影响]
数据库可能通过锁或并发冲突检测实现此级别，并不等同于全库排他锁。需要考虑等待、死锁或序列化失败；发生可重试冲突时，应按数据库要求重试整个事务。详见 [PostgreSQL 事务隔离说明](https://www.postgresql.org/docs/current/transaction-iso.html)。
:::

## 各级别对比

下表描述标准允许的现象；数据库可以提供更强保证，例如 PostgreSQL 的 Repeatable Read 不允许幻读。

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
|---|:---:|:---:|:---:|
| READ_UNCOMMITTED | 可能 | 可能 | 可能 |
| READ_COMMITTED | - | 可能 | 可能 |
| REPEATABLE_READ | - | - | 可能 |
| SERIALIZABLE | - | - | - |

## 深入阅读

- [注解式事务](./annotation)：在 `@Transactional` 上指定隔离级别。
- [模板事务](./template)：在 `execute` 的第三个参数中指定隔离级别。
- [事务管理器](./manager)：理解隔离级别如何在连接上设置并在事务结束后恢复。
