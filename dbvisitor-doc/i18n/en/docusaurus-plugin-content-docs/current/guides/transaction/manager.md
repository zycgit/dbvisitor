---
id: manager
sidebar_position: 4
title: 10.4 Transaction Manager
description: Understand TransactionManager, TransactionStatus, the transaction stack, suspension, and savepoints.
---

# Transaction Manager

`TransactionManager` is the core transaction interface. Annotation-based transactions and templates ultimately call its `begin`, `commit`, and `rollBack` methods.

## Interface Capabilities

| Method | Purpose |
|---|---|
| `begin()` | Begin a transaction; defaults to `Propagation.REQUIRED` + `Isolation.DEFAULT` |
| `begin(Propagation)` | Specify propagation with default isolation |
| `begin(Propagation, Isolation)` | Specify propagation and isolation |
| `commit()` | Commit the most recent transaction |
| `commit(TransactionStatus)` | Commit the specified transaction status |
| `rollBack()` | Roll back the most recent transaction |
| `rollBack(TransactionStatus)` | Roll back the specified transaction status |
| `hasTransaction()` | Whether this manager has unfinished transactions |
| `isTopTransaction(TransactionStatus)` | Whether the specified transaction is at the top of the stack |

Each `begin(...)` returns a `TransactionStatus` describing that transaction scope:

| Method | Meaning |
|---|---|
| `getPropagation()` | Propagation for this transaction |
| `getIsolationLevel()` | Isolation for this transaction |
| `isCompleted()` | Whether it has committed or rolled back |
| `isRollbackOnly()` | Whether marked for rollback |
| `isReadOnly()` | Whether marked read-only |
| `isNewConnection()` | Whether a new database connection was opened |
| `isSuspend()` | Whether an existing transaction was suspended |
| `hasSavepoint()` | Whether a savepoint was created |
| `setRollback()` | Request rollback on commit |
| `setReadOnly()` | Mark read-only; commit performs rollback |

## Local Transaction Manager

The default implementation is `LocalTransactionManager`:

```java title='Create a Local Transaction Manager'
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

TransactionManager txManager = new LocalTransactionManager(dataSource);
```

Usually, obtain the manager through `TransactionHelper`:

```java title='Reuse One Transaction Manager per DataSource'
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

`LocalTransactionManager` binds to one `DataSource` and reuses the current transaction connection through thread context. JdbcTemplate and Mapper operations accessing that DataSource inside a transaction obtain the connection bound to the current thread.

## Transaction Stack

A manager can call `begin` repeatedly. Each call produces a `TransactionStatus` and pushes it onto the transaction stack.

```java title='Begin Three Transaction Scopes'
TransactionStatus tranA = txManager.begin();
TransactionStatus tranB = txManager.begin();
TransactionStatus tranC = txManager.begin();
```

The stack looks like this:

```text
Stack top
  |
  v
+--------+
| Tran C |
+--------+
| Tran B |
+--------+
| Tran A |
+--------+
```

Normally, finish in reverse order:

```java
txManager.commit(tranC);
txManager.commit(tranB);
txManager.commit(tranA);
```

Committing `tranA` directly first processes transactions opened after it, then processes `tranA`:

```java title='Commit an Outer Scope'
txManager.commit(tranA);
```

Equivalent to:

```java
txManager.commit(tranC);
txManager.commit(tranB);
txManager.commit(tranA);
```

This avoids leaving entries on the stack, but explicitly committing or rolling back from the top remains clearer.

## How Propagation Affects Connections

`begin(Propagation, Isolation)` decides whether to open, reuse, or suspend a connection or create a savepoint based on the current thread's transaction context.

| Propagation | With an Existing Transaction | Connection / Savepoint Effect |
|---|---|---|
| `REQUIRED` | Join existing | Reuse current connection; inner commit does not commit the database transaction |
| `REQUIRES_NEW` | Suspend existing and begin new | New connection; restore outer connection on completion |
| `NESTED` | Create nested scope | Create a savepoint on the current connection |
| `SUPPORTS` | Join existing | Reuse current connection |
| `NOT_SUPPORTED` | Suspend existing and run non-transactionally | Temporarily clear the transaction connection |
| `NEVER` | Throw an exception | Must not run within a transaction |
| `MANDATORY` | Join existing | Throw if no transaction exists |

## Commit, Rollback, and Read-Only Markers

TransactionTemplateManager and TransactionInterceptor follow the same rule: if TransactionStatus is marked for rollback or read-only, the final commit call performs rollback.

```java title='Call commit after Marking Rollback'
TransactionStatus tran = txManager.begin();
try {
    jdbcTemplate.executeUpdate(
            "update sku_stock set quantity = quantity - ? where sku_id = ?",
            new Object[] { quantity, skuId }
    );

    if (quantity <= 0) {
        tran.setRollback();
    }

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

If `quantity <= 0`, `commit(tran)` follows the rollback path.

## Relationship to the Three APIs

```text
@Transactional
  -> TransactionInterceptor
      -> TransactionManager.begin(...)
      -> TransactionManager.commit(...) or rollback

TransactionTemplate.execute(...)
  -> TransactionTemplateManager
      -> TransactionManager.begin(...)
      -> TransactionManager.commit(...) or rollback

Programmatic Transactions
  -> Business code calls TransactionManager directly
```

Start with [Annotation-Based Transactions](./annotation) and [Transaction Templates](./template). Return here to understand connection suspension, savepoints, or stack ordering.
