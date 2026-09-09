---
id: propagation
sidebar_position: 5
title: 10.5 Propagation
description: Transaction propagation in dbVisitor.
---

# Transaction Propagation

When transactional methods call one another on the same thread, propagation determines how their transaction scopes interact.

Propagation can be specified through all three transaction APIs:

```java title='Annotation-Based Transactions'
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void writeAuditLog(long orderId) {
    ...
}
```

```java title='Transaction Templates'
txTemplate.execute(tranStatus -> {
    ...
    return null;
}, Propagation.NESTED);
```

```java title='Programmatic Transactions'
TransactionStatus tran = txManager.begin(Propagation.REQUIRED);
```

## How to Choose

| Desired Effect | Recommended Propagation |
|---|---|
| Most business methods: join existing or begin new | `REQUIRED` |
| Audit and operation logs: commit independently | `REQUIRES_NEW` |
| Allow one step to fail while the outer transaction continues | `NESTED` |
| Join existing but also allow non-transactional execution | `SUPPORTS` |
| Run a block outside the current transaction | `NOT_SUPPORTED` |
| Forbid transactional execution | `NEVER` |
| Require an outer transaction | `MANDATORY` |

## Join Existing Transaction (REQUIRED)

Join an existing transaction or begin a new one if none exists.
- Constant `Propagation.REQUIRED`

```java title='Order Creation Uses REQUIRED by Default'
@Transactional
public void createOrder(long orderId) {
    orderMapper.insertOrder(orderId);
    orderMapper.insertOrderItems(orderId);
}
```

REQUIRED is the default: it begins a transaction if none exists or joins the outer transaction. Normal return from an inner method does not commit immediately; the outermost scope decides the outcome. In dbVisitor's local manager, inner REQUIRED rollback does not automatically mark the outer scope rollback-only. If the outer scope catches and suppresses the exception, these writes may still commit. To fail the entire operation, propagate the exception or explicitly mark the outer scope for rollback.

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | begin | | Begin Transaction A |
| T2 | insert data1 | | |
| T3 | | begin | Join transaction A (no database action) |
| T4 | | insert data2 | |
| T5 | | commit/rollback | No database action (outer scope decides) |
| T6 | insert data3 | | |
| T7 | commit/rollback | | Commit/rollback Transaction A |

## Independent Transaction (REQUIRES_NEW)

Suspend any existing transaction and begin a new, independent transaction.
- Constant `Propagation.REQUIRES_NEW`

```java title='Audit Logs Can Commit Despite Outer Failure'
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void writeAuditLog(long orderId, String action) throws java.sql.SQLException {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            new Object[] { orderId, action }
    );
}
```

:::info
- Suspension temporarily makes the thread-bound Connection unavailable.
- The manager then creates a new Connection for the current thread.
:::

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | begin | | Begin Transaction A |
| T2 | insert data1 | | |
| T3 | | begin | Suspend A → new Connection B → begin B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | Commit/rollback B → resume A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | Commit/rollback Transaction A |

## Nested Transaction (NESTED)

Create a nested scope using a Savepoint on the current transaction. Nested rollback does not end the outer transaction; outer rollback also undoes nested work.
- Constant `Propagation.NESTED`

```java title='Roll Back Only the Coupon Work on Failure'
txTemplate.execute(tranStatus -> {
    couponMapper.bindCoupon(orderId, couponId);
    return null;
}, Propagation.NESTED);
```

NESTED requires database and driver Savepoint support. Use it when a local step may fail within a larger transaction.

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | begin | | Begin Transaction A |
| T2 | insert data1 | | |
| T3 | | begin | Create Savepoint B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | Release/rollback Savepoint B |
| T6 | insert data3 | | |
| T7 | commit/rollback | | Commit/rollback Transaction A |

## Follow Current Context (SUPPORTS)

Run non-transactionally when no transaction exists; otherwise join it, as with REQUIRED.
- Constant `Propagation.SUPPORTS`

```java title='Queries Follow the Caller Context'
@Transactional(propagation = Propagation.SUPPORTS)
public OrderInfo queryOrder(long orderId) {
    return orderMapper.queryOrder(orderId);
}
```

:::info
SUPPORTS neither starts a transaction nor prevents one.
:::

## Non-Transactional (NOT_SUPPORTED)

Run non-transactionally; suspend any existing transaction first.
- Constant `Propagation.NOT_SUPPORTED`

```java title='Run Large Queries Outside the Outer Transaction'
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public List<OrderReport> queryReport() {
    return reportMapper.queryOrderReport();
}
```

| Time | Transaction A | Transaction B | Effect |
|---|---|---|---|
| T1 | begin | | Begin Transaction A |
| T2 | insert data1 | | |
| T3 | | begin | Suspend Transaction A |
| T4 | | insert data2 | Run non-transactionally |
| T5 | | commit/rollback | Resume Transaction A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | Commit/rollback Transaction A |

## Exclude Transactions (NEVER)

Run non-transactionally if no transaction exists; otherwise throw an exception.
- Constant `Propagation.NEVER`

```java title='Forbid Calls Within a Transaction'
@Transactional(propagation = Propagation.NEVER)
public void rebuildSearchIndex() {
    ...
}
```

## Require a Transaction (MANDATORY)

Join an existing transaction; throw an exception if none exists.
- Constant `Propagation.MANDATORY`

```java title='Require an Outer Transaction'
@Transactional(propagation = Propagation.MANDATORY)
public void insertOrderItem(long orderId, long skuId) {
    orderMapper.insertOrderItem(orderId, skuId);
}
```

## Propagation Comparison

| Propagation | No Transaction | Existing Transaction |
|---|---|---|
| `REQUIRED` | Begin new | Join existing |
| `REQUIRES_NEW` | Begin new | Suspend existing → Begin new |
| `NESTED` | Begin new | Savepoint nested scope |
| `SUPPORTS` | Run non-transactionally | Join existing |
| `NOT_SUPPORTED` | Run non-transactionally | Suspend existing → Run non-transactionally |
| `NEVER` | Run non-transactionally | Throw exception |
| `MANDATORY` | Throw exception | Join existing |

## Further Reading

- [Annotation-Based Transactions](./annotation): Set propagation on @Transactional.
- [Transaction Templates](./template): Set propagation in the second execute argument.
- [Transaction Manager](./manager): Understand REQUIRES_NEW connection suspension and NESTED savepoints.
