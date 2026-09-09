---
id: program
sidebar_position: 3
title: 10.3 Programmatic Transactions
description: Use TransactionManager directly to begin, commit, and roll back transactions.
---

# Programmatic Transactions

Programmatic transactions use TransactionManager directly. This provides the most flexibility but is also easier to misuse; use it when precise control over transaction state is needed.

## Suitable For

- You need to retain TransactionStatus manually.
- One method must explicitly begin and finish multiple transactions.
- You need to inspect state through hasTransaction() or isTopTransaction(...).

## Not Suitable For

- For regular Service methods, prefer [Annotation-Based Transactions](./annotation).
- To wrap a code block, prefer [Transaction Templates](./template).

The following snippets belong in methods that declare the required exceptions. JdbcTemplate and the transaction manager must use the same DataSource.

## Basic Usage

```java title='Manual Commit and Rollback'
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

TransactionManager txManager = TransactionHelper.txManager(dataSource);

TransactionStatus tran = txManager.begin();
try {
    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            new Object[] { orderId, userId }
    );
    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            new Object[] { orderId, skuId }
    );

    txManager.commit(tran);
} catch (Throwable e) {
    if (!tran.isCompleted()) {
        try {
            txManager.rollBack(tran);
        } catch (Throwable rollbackError) {
            e.addSuppressed(rollbackError);
        }
    }
    throw e;
}
```

Behavior:
- If both statements succeed, commit(tran) commits the transaction.
- If SQL fails and the transaction is unfinished, attempt rollback. A failed commit does not mean rollback succeeded; inspect the exception and database state to determine the outcome.

## Specify Propagation and Isolation

```java title='Begin an Independent Transaction'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;

TransactionStatus auditTran = txManager.begin(
        Propagation.REQUIRES_NEW,
        Isolation.READ_COMMITTED
);
try {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            new Object[] { orderId, "CREATE" }
    );
    txManager.commit(auditTran);
} catch (Throwable e) {
    if (!auditTran.isCompleted()) {
        try {
            txManager.rollBack(auditTran);
        } catch (Throwable rollbackError) {
            e.addSuppressed(rollbackError);
        }
    }
    throw e;
}
```

REQUIRES_NEW suspends any existing transaction on the current thread and creates a new connection for this transaction.

## Roll Back Only a Nested Scope

NESTED creates a savepoint within an existing transaction. Inner rollback returns to that savepoint without directly ending the outer transaction.

```java title='Use NESTED to Roll Back Local Work'
TransactionStatus outer = txManager.begin();
try {
    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            new Object[] { orderId, userId }
    );

    TransactionStatus nested = txManager.begin(Propagation.NESTED);
    try {
        jdbcTemplate.executeUpdate(
                "insert into order_coupon(order_id, coupon_id) values(?, ?)",
                new Object[] { orderId, couponId }
        );
        txManager.commit(nested);
    } catch (SQLException couponError) {
        if (nested.isCompleted()) {
            throw couponError;
        }
        try {
            txManager.rollBack(nested);
        } catch (Throwable rollbackError) {
            couponError.addSuppressed(rollbackError);
            throw couponError;
        }
        // Continue only if business rules allow this coupon failure; record its cause in a real application.
    }

    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            new Object[] { orderId, skuId }
    );
    txManager.commit(outer);
} catch (Throwable e) {
    if (!outer.isCompleted()) {
        try {
            txManager.rollBack(outer);
        } catch (Throwable rollbackError) {
            e.addSuppressed(rollbackError);
        }
    }
    throw e;
}
```

Behavior:
- If the coupon write fails, only its order_coupon savepoint is rolled back.
- The outer order and items may still commit.
- If the outer transaction rolls back, successful nested work rolls back with it.

## Obtain TransactionManager

```java title='Method 1: Obtain via TransactionHelper'
TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

TransactionHelper.txManager(dataSource) reuses the same manager instance for a DataSource.

```java title='Method 2: Create LocalTransactionManager'
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

TransactionManager txManager = new LocalTransactionManager(dataSource);
```

```java title='Method 3: Dependency Injection'
public class OrderService {
    // @Inject                 < Guice, Solon and Hasor
    // @Resource or @Autowired < Spring
    private TransactionManager txManager;
}
```

## Usage Notes

- Call commit(...) or rollBack(...) once on an unfinished transaction.
- If the target is not at the top of the stack, dbVisitor first processes scopes opened after it.
- TransactionManager implements Closeable, but explicit commit or rollBack calls make ordinary transaction boundaries clearer.

## Further Reading

- [Transaction Manager](./manager): Understand how the stack affects commit and rollback order.
- [Propagation](./propagation): Understand each Propagation value's effect on connections, savepoints, and suspension.
- [Isolation](./isolation): Understand how Isolation maps to JDBC connection isolation.
