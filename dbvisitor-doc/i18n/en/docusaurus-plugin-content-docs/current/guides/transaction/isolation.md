---
id: isolation
sidebar_position: 6
title: 10.6 Isolation Levels
description: Transaction isolation levels in dbVisitor.
---

<span id="isolation-levels" />

# 10.6 Isolation Levels

Isolation determines what concurrent transactions can observe when reading and writing the same data. dbVisitor's Isolation maps to JDBC Connection#setTransactionIsolation(int).

Isolation can be specified through all three transaction APIs:

```java title='Annotation-Based Transactions'
@Transactional(isolation = Isolation.READ_COMMITTED)
public void createOrder(long orderId) {
    ...
}
```

```java title='Transaction Templates'
txTemplate.execute(tranStatus -> {
    ...
    return null;
}, Propagation.REQUIRED, Isolation.REPEATABLE_READ);
```

```java title='Programmatic Transactions'
TransactionStatus tran = txManager.begin(
        Propagation.REQUIRED,
        Isolation.SERIALIZABLE
);
```

## How to Choose

| Desired Effect | Common Isolation Level |
|---|---|
| Use the database default | `DEFAULT` |
| Avoid uncommitted reads while allowing concurrency | `READ_COMMITTED` |
| Keep repeated reads of existing rows consistent | `REPEATABLE_READ` |
| Require the strongest consistency and accept concurrency costs | `SERIALIZABLE` |
| Explicitly allow dirty reads and weak consistency | `READ_UNCOMMITTED` |

:::info
Defaults and exact semantics differ by database. MySQL, PostgreSQL, and Oracle differ in support for REPEATABLE_READ and handling of phantom reads and locks. dbVisitor sets JDBC isolation but cannot change the database transaction model.
:::

The examples use this table:

```sql
mysql> select * from students;
+----+-------+
| id | name  |
+----+-------+
|  1 | Alice |
+----+-------+
```

## DEFAULT

Use the default isolation level determined by the database driver.
- Constant `Isolation.DEFAULT`

```java title='Use Default Isolation'
@Transactional
public void createOrder(long orderId) {
    ...
}
```

## Read Uncommitted

At the lowest isolation level, a transaction may read another transaction's uncommitted updates. If the writer rolls back, the reader has observed dirty data.
- Constant `Isolation.READ_UNCOMMITTED`

```java title='Explicitly Allow READ_UNCOMMITTED'
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public List<OrderInfo> queryFastButWeakConsistent() {
    return orderMapper.queryRecentOrders();
}
```

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | | set isolation level read uncommitted | |
| T2 | begin | begin | |
| T3 | update students set name='bob' where id=1 | | |
| T4 | | select * from students where id=1 | Reads 'bob' (dirty read) |
| T5 | rollback | | |
| T6 | | select * from students where id=1 | Reads 'Alice' again |

## Read Committed

Only committed changes from other transactions are visible. Repeated queries within one transaction may return different results if another transaction commits changes between them.
- Constant `Isolation.READ_COMMITTED`

```java title='Use READ_COMMITTED for Business Writes'
@Transactional(isolation = Isolation.READ_COMMITTED)
public void payOrder(long orderId) {
    orderMapper.markPaid(orderId);
    orderMapper.insertPayLog(orderId);
}
```

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | set isolation level read committed | set isolation level read committed | |
| T2 | begin | begin | |
| T3 | | select * from students where id=1 | Reads 'Alice' |
| T4 | update students set name='bob' where id=1 | | |
| T5 | | select * from students where id=1 | Still reads 'Alice' (uncommitted changes invisible) |
| T6 | commit | | |
| T7 | | select * from students where id=1 | Reads 'bob' (non-repeatable read) |
| T8 | | commit | |

## Repeatable Read

Protects repeated reads of existing rows against committed modifications by other transactions. Behavior for own writes, new rows in a range, and locking reads remains database-specific.
- Constant `Isolation.REPEATABLE_READ`

```java title='Stable Reads Within One Transaction'
@Transactional(isolation = Isolation.REPEATABLE_READ)
public OrderSummary buildOrderSummary(long orderId) {
    Order order = orderMapper.queryOrder(orderId);
    List<OrderItem> items = orderMapper.queryItems(orderId);
    return new OrderSummary(order, items);
}
```

:::info[Phantom Read]
Repeatable Read may allow phantom reads:
Phantom reads usually mean rows appearing or disappearing from repeated range queries due to other commits. The example below shows how MySQL InnoDB consistent reads and updates may observe different data; this is not universal behavior at this level.
:::

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | set isolation level repeatable read | set isolation level repeatable read | |
| T2 | begin | begin | |
| T3 | | select * from students where id=99 | Empty result |
| T4 | insert into students (id, name) values (99, 'bob') | | |
| T5 | commit | | |
| T6 | | select * from students where id=99 | Still empty (repeatable read) |
| T7 | | update students set name='alice' where id=99 | Update succeeds |
| T8 | | select * from students where id=99 | Data appears after the update |

## Serializable

Committed transactions must have an effect equivalent to some serial ordering; transactions do not have to run one at a time. This excludes dirty reads, non-repeatable reads, phantom reads, and non-serializable outcomes.
- Constant `Isolation.SERIALIZABLE`

```java title='Use When the Strongest Consistency Is Required'
@Transactional(isolation = Isolation.SERIALIZABLE)
public void allocateUniqueNumber(String bizType) {
    Long nextNumber = numberMapper.queryNextNumber(bizType);
    numberMapper.updateNextNumber(bizType, nextNumber + 1);
}
```

:::caution[Performance Impact]
Databases may implement this level with locks or concurrency-conflict detection; it does not mean a database-wide exclusive lock. Account for waits, deadlocks, or serialization failures. Retry the whole transaction when required by the database for a retryable conflict. See [PostgreSQL Transaction Isolation](https://www.postgresql.org/docs/current/transaction-iso.html)。
:::

## Isolation Comparison

The table describes phenomena allowed by the standard. Databases may provide stronger guarantees; for example, PostgreSQL Repeatable Read does not allow phantom reads.

| Isolation Levels | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|:---:|:---:|:---:|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | - | Possible | Possible |
| REPEATABLE_READ | - | - | Possible |
| SERIALIZABLE | - | - | - |

## Further Reading

- [Annotation-Based Transactions](./annotation): Set isolation on @Transactional.
- [Transaction Templates](./template): Set isolation in the third execute argument.
- [Transaction Manager](./manager): Understand how isolation is set on the connection and restored after the transaction.
