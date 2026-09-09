---
id: template
sidebar_position: 2
title: 10.2 Transaction Templates
description: Use TransactionTemplate to commit or roll back a code block automatically.
---

# Transaction Templates

Transaction templates wrap a local code block with standard transaction control: begin first, commit on normal completion, and roll back on an exception or rollback marker.

## Suitable For

- Wrap a code block in a plain Java program.
- Avoid creating proxies for `@Transactional`.
- Avoid repeating try/catch/finally.

## Not Suitable For

- A Service method defines the boundary and proxies are already available.
- Multiple transaction statuses need manual commit or rollback ordering.

This page uses dbVisitor's transaction template, not Spring's class of the same name. `execute` declares `Throwable`; callers must catch or declare it. Sequence SQL examples apply to databases supporting `NEXT VALUE FOR`, such as H2.

## Basic Usage

```java title='Create TransactionTemplate'
import javax.sql.DataSource;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

DataSource dataSource = ...;
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate txTemplate = new TransactionTemplateManager(txManager);
```

```java title='Return a Result from a Transaction'
Long orderId = txTemplate.execute(tranStatus -> {
    Long newOrderId = jdbcTemplate.queryForObject(
            "select next value for seq_order",
            Long.class
    );

    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            new Object[] { newOrderId, userId }
    );
    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            new Object[] { newOrderId, skuId }
    );
    return newOrderId;
});
```

Behavior:
- On normal callback return, the template commits and returns `newOrderId` from `execute`.
- When the callback throws, the template marks the transaction for rollback and rethrows.

## Code Blocks Without Results

```java title='Use TransactionCallbackWithoutResult'
import net.hasor.dbvisitor.transaction.TransactionCallbackWithoutResult;

txTemplate.execute((TransactionCallbackWithoutResult) tranStatus -> {
    jdbcTemplate.executeUpdate(
            "delete from order_item where order_id = ?",
            orderId
    );
    jdbcTemplate.executeUpdate(
            "delete from orders where id = ?",
            orderId
    );
});
```

## Request Rollback

To request rollback without throwing an exception, call `tranStatus.setRollback()` in the callback.

```java title='Roll Back on Validation Failure and Return a Business Result'
Boolean success = txTemplate.execute(tranStatus -> {
    int updated = jdbcTemplate.executeUpdate(
            "update sku_stock set quantity = quantity - ? where sku_id = ? and quantity >= ?",
            new Object[] { quantity, skuId, quantity }
    );

    if (updated == 0) {
        tranStatus.setRollback();
        return false;
    }

    jdbcTemplate.executeUpdate(
            "insert into stock_log(sku_id, quantity) values(?, ?)",
            new Object[] { skuId, quantity }
    );
    return true;
});
```

Behavior:
- With sufficient stock, the stock deduction and log commit together; the result is `true`.
- With insufficient stock, the transaction rolls back without throwing and returns `false`.

## Specify Propagation and Isolation

```java title='Write Audit Logs in an Independent Transaction'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;

txTemplate.execute(tranStatus -> {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            new Object[] { orderId, "CREATE" }
    );
    return null;
}, Propagation.REQUIRES_NEW, Isolation.READ_COMMITTED);
```

Inside an outer transaction, `REQUIRES_NEW` suspends it and opens a new connection for the audit transaction. The outer transaction resumes after the audit transaction commits or rolls back.

## Obtain TransactionTemplate

```java title='Plain Java Programs'
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate txTemplate = new TransactionTemplateManager(txManager);
```

```java title='Dependency Injection'
public class OrderService {
    // @Inject                 < Guice, Solon and Hasor
    // @Resource or @Autowired < Spring
    private TransactionTemplate txTemplate;
}
```

Related integrations:
- [Spring Integration](../yourproject/with_spring#tran)
- [Solon Integration](../yourproject/with_solon#tran)
- [Guice Injectable Types](../yourproject/with_guice#inject)
- [Hasor Injectable Types](../yourproject/with_hasor#inject)

## Further Reading

- [Transaction Manager](./manager): How the template calls `TransactionManager`.
- [Propagation](./propagation): Choose the template's second argument.
- [Isolation](./isolation): Choose the template's third argument.
