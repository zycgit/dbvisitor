---
id: about
sidebar_position: 0
title: Transactions
description: dbVisitor provides local transaction support through annotations, templates, programmatic APIs, propagation behavior, and isolation levels.
---

# Transactions

dbVisitor provides lightweight local transaction support for grouping multiple database operations into one commit or rollback boundary. It is useful for workflows such as creating an order and its items together, writing a master row plus detail rows, or running one step in an independent transaction.

A transaction is managed by `TransactionManager`, and one manager is bound to one `DataSource`. Inside a transaction, dbVisitor lets `JdbcTemplate`, Mapper, BaseMapper, LambdaTemplate, and other APIs reuse the database connection bound to the current thread. The transaction is then committed or rolled back at the end.

:::info
dbVisitor manages local transactions, not distributed transactions. Multiple `DataSource` instances can each have transaction interceptors, but dbVisitor does not add two-phase commit between databases.
:::

## Choose an API

| API | Best for | Not best for | Entry |
|---|---|---|---|
| Annotation-based | Clear Service method boundaries and clean business code | No proxy/interceptor, or dynamic transaction boundaries | [Annotation-based](./annotation) |
| Transaction template | A local code block that should commit or roll back automatically | Manually controlling multiple transaction statuses | [Transaction template](./template) |
| Programmatic | Full control over `begin`, `commit`, and `rollBack` | Regular business methods with repetitive try/catch code | [Programmatic](./program) |

Typical recommendation:
- In Spring, Solon, Guice, or Hasor projects, prefer annotation-based transactions.
- In plain Java programs, scripts, or local transaction blocks, prefer transaction templates.
- Use programmatic transactions only when you need to control transaction statuses directly.

## A Complete Effect

The following method makes order creation atomic: order, item, and stock update must succeed together. If any step fails, all steps roll back.

```java title='OrderService.java'
import net.hasor.dbvisitor.jdbc.JdbcTemplate;
import net.hasor.dbvisitor.transaction.Transactional;

public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public long createOrder(long userId, long skuId, int quantity) {
        Long orderId = jdbcTemplate.queryForObject(
                "select next value for seq_order",
                Long.class
        );

        jdbcTemplate.executeUpdate(
                "insert into orders(id, user_id) values(?, ?)",
                orderId, userId
        );
        jdbcTemplate.executeUpdate(
                "insert into order_item(order_id, sku_id, quantity) values(?, ?, ?)",
                orderId, skuId, quantity
        );
        jdbcTemplate.executeUpdate(
                "update sku_stock set quantity = quantity - ? where sku_id = ?",
                quantity, skuId
        );
        return orderId;
    }
}
```

If the stock update throws after the order item is inserted, the interceptor marks the transaction for rollback. The final effect is that no partial order data is committed.

## Code Structure

Transaction-related APIs live under `net.hasor.dbvisitor.transaction`:

| Type | Purpose |
|---|---|
| `@Transactional` | Declares a method or class as transactional |
| `Propagation` | Controls how a method joins, creates, or suspends transactions |
| `Isolation` | Controls database isolation level |
| `TransactionManager` | Core `begin/commit/rollBack` API |
| `TransactionStatus` | State returned by one transaction begin operation |
| `TransactionTemplate` | Callback-style transaction boundary |
| `TransactionTemplateManager` | Default `TransactionTemplate` implementation |
| `TransactionHelper` | Creates managers from `DataSource` or annotation proxies |
| `LocalTransactionManager` | Default local transaction manager implementation |

```text
DataSource
  |
  v
LocalTransactionManager  -- begin/commit/rollBack --> TransactionStatus
  |
  +-- TransactionTemplateManager.execute(...)
  |
  +-- TransactionInterceptor --> @Transactional
```

## Learn More

- [Annotation-based transactions](./annotation)
- [Transaction template](./template)
- [Programmatic transactions](./program)
- [Transaction manager](./manager)
- [Propagation behavior](./propagation)
- [Isolation levels](./isolation)
