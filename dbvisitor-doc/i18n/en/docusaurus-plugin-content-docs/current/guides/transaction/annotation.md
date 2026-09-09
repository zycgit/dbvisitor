---
id: annotation
sidebar_position: 1
title: 10.1 Annotation-Based Transactions
description: Declare transaction boundaries on methods or classes with @Transactional.
---

# Annotation-Based Transactions

Annotation-based transactions place boundaries on methods or classes, removing manual `begin`, `commit`, and `rollBack` calls from business code. This is the usual approach in the Service layer.

## Suitable For

- A business method naturally defines a transaction boundary.
- Automatic rollback on exceptions and commit on normal return are desired.
- The project uses Spring, Solon, Guice, or Hasor, or can create proxies with `TransactionHelper.support()`.

## Not Suitable For

- Methods are not invoked through a proxy.
- One method must start multiple transactions according to runtime conditions.
- You need manual control over the commit or rollback order of transaction statuses.

## Basic Usage

```java title='OrderService.java'
import net.hasor.dbvisitor.jdbc.JdbcTemplate;
import net.hasor.dbvisitor.transaction.Transactional;

public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createOrder(long orderId, long skuId) throws java.sql.SQLException {
        jdbcTemplate.executeUpdate(
                "insert into orders(id) values(?)",
                orderId
        );
        jdbcTemplate.executeUpdate(
                "insert into order_item(order_id, sku_id) values(?, ?)",
                new Object[] { orderId, skuId }
        );
    }
}
```

Behavior:
- When `createOrder` returns normally, both inserts commit together.
- If either SQL statement throws, the transaction is marked for rollback and neither insert commits.

## Enable Annotation-Based Transactions

`@Transactional` is only a marker; an interceptor or proxy must handle the invocation for it to take effect.

### Plain Java Programs

```java title='Create a proxy with TransactionHelper.support'
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

DataSource dataSource = ...;
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

OrderService target = new OrderService(jdbcTemplate);
OrderService orderService = TransactionHelper.support(target, dataSource);

orderService.createOrder(10001L, 20001L);
```

`TransactionHelper.support(target, dataSource)` creates a proxy for `target`. Only calls through that proxy trigger transaction interception.

:::tip
Do not expect `this.createOrder(...)` within the class to trigger a transaction; self-invocation does not pass through the proxy.
:::

This page uses dbVisitor's own `@Transactional`. When using Spring annotations and transaction managers, follow Spring's rollback rules; do not mix the two annotation systems.

### Framework Projects

- Spring projects can use Spring transaction management; [Spring integration](../yourproject/with_spring#tran)。
- Solon projects can enable transactions through the integration; [Solon integration](../yourproject/with_solon#tran)。
- Guice and Hasor projects can inject dbVisitor transaction objects; [Guice integration](../yourproject/with_guice#inject), [Hasor integration](../yourproject/with_hasor#inject)。

## Specify Propagation and Isolation

```java title='Start an independent transaction with READ_COMMITTED isolation'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.Transactional;

@Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED
)
public void writeAuditLog(long orderId, String action) throws java.sql.SQLException {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            new Object[] { orderId, action }
    );
}
```

When invoked within an outer transaction, this method suspends it and starts an independent transaction on a new connection. This suits audit logs or operation records that should commit independently even if the outer transaction fails.

## Control Rollback Rules

By default, exceptions from business methods trigger rollback. Use `noRollbackFor` or `noRollbackForClassName` to exclude specific exceptions:

```java title='Retain the attempt record when stock validation fails'
@Transactional(noRollbackFor = { IllegalArgumentException.class })
public void reserveStock(long skuId, int quantity) throws java.sql.SQLException {
    jdbcTemplate.executeUpdate(
            "insert into stock_try_log(sku_id, quantity) values(?, ?)",
            new Object[] { skuId, quantity }
    );

    if (quantity <= 0) {
        throw new IllegalArgumentException("quantity must be positive");
    }
}
```

`IllegalArgumentException` still propagates, but this exception does not mark the transaction for rollback.

## Annotation Attributes

| Attribute | Type | Default | Description |
|---|---|---|---|
| `propagation` | `Propagation` | `REQUIRED` | How to join, create, or suspend transactions |
| `isolation` | `Isolation` | `DEFAULT` | Database isolation level |
| `readOnly` | `boolean` | `false` | Mark read-only; dbVisitor rolls back when commit is requested |
| `noRollbackFor` | `Class<? extends Throwable>[]` | `{}` | Exception types that do not trigger rollback |
| `noRollbackForClassName` | `String[]` | `{}` | Fully qualified exception names that do not trigger rollback |

## Further Reading

- [Propagation](./propagation): Choose between `REQUIRED`, `REQUIRES_NEW`, and `NESTED`.
- [Isolation](./isolation): Decide when to set an explicit isolation level.
- [Transaction Manager](./manager): Understand how annotations begin, commit, and roll back transactions.
