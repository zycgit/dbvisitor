---
id: annotation
sidebar_position: 1
title: 10.1 注解式事务
description: 使用 @Transactional 在方法或类上声明事务边界。
---

# 注解式事务

注解式事务把事务边界放在方法或类上，业务代码里不需要手动写 `begin`、`commit`、`rollBack`。这是 Service 层最常用的事务写法。

## 适合

- 一个业务方法天然就是一个事务边界。
- 希望异常时自动回滚，正常返回时自动提交。
- 项目已经使用 Spring、Solon、Guice、Hasor，或者愿意用 `TransactionHelper.support()` 创建代理对象。

## 不适合

- 方法不是通过代理对象调用。
- 同一个方法里需要根据运行时条件动态开启多个事务。
- 想手动控制某个 `TransactionStatus` 的提交或回滚顺序。

## 基本用法

```java title='OrderService.java'
import net.hasor.dbvisitor.jdbc.JdbcTemplate;
import net.hasor.dbvisitor.transaction.Transactional;

public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createOrder(long orderId, long skuId) {
        jdbcTemplate.executeUpdate(
                "insert into orders(id) values(?)",
                orderId
        );
        jdbcTemplate.executeUpdate(
                "insert into order_item(order_id, sku_id) values(?, ?)",
                orderId, skuId
        );
    }
}
```

执行效果：
- `createOrder` 正常返回时，两个 `insert` 一起提交。
- 任意 SQL 抛出异常时，事务被标记为回滚，两个 `insert` 都不会提交。

## 启用注解事务

`@Transactional` 本身只是标记，必须通过拦截器或代理对象调用才会生效。

### 普通 Java 程序

```java title='通过 TransactionHelper.support 创建代理对象'
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

DataSource dataSource = ...;
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

OrderService target = new OrderService(jdbcTemplate);
OrderService orderService = TransactionHelper.support(target, dataSource);

orderService.createOrder(10001L, 20001L);
```

`TransactionHelper.support(target, dataSource)` 会为 `target` 创建代理对象。只有调用代理对象上的方法时，`@Transactional` 才会触发事务拦截。

:::tip
不要在类内部用 `this.createOrder(...)` 期待触发事务；这种自调用不会经过代理对象。
:::

### 框架项目

- Spring 项目可以直接使用 Spring 的事务体系，[查看 Spring 集成](../yourproject/with_spring#tran)。
- Solon 项目可以通过 Solon 集成启用事务，[查看 Solon 集成](../yourproject/with_solon#tran)。
- Guice 和 Hasor 项目可以注入 dbVisitor 提供的事务对象，[查看 Guice 集成](../yourproject/with_guice#inject)、[查看 Hasor 集成](../yourproject/with_hasor#inject)。

## 指定传播行为和隔离级别

```java title='开启一个独立事务，并使用 READ_COMMITTED 隔离级别'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.Transactional;

@Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED
)
public void writeAuditLog(long orderId, String action) {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            orderId, action
    );
}
```

这个方法如果在外层事务中被调用，会挂起外层事务，使用新的连接开启独立事务。它适合写审计日志、操作记录这类“外层失败也希望单独提交”的场景。

## 控制回滚规则

默认情况下，业务方法抛出异常会触发回滚。可以通过 `noRollbackFor` 或 `noRollbackForClassName` 指定某些异常不回滚：

```java title='库存不足时不回滚已写入的尝试记录'
@Transactional(noRollbackFor = { IllegalArgumentException.class })
public void reserveStock(long skuId, int quantity) {
    jdbcTemplate.executeUpdate(
            "insert into stock_try_log(sku_id, quantity) values(?, ?)",
            skuId, quantity
    );

    if (quantity <= 0) {
        throw new IllegalArgumentException("quantity must be positive");
    }
}
```

`IllegalArgumentException` 会继续向外抛出，但事务不会因为这个异常被标记为回滚。

## 注解属性

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `propagation` | `Propagation` | `REQUIRED` | 如何加入、创建或挂起事务 |
| `isolation` | `Isolation` | `DEFAULT` | 使用哪种数据库隔离级别 |
| `readOnly` | `boolean` | `false` | 标记只读；在 dbVisitor 中提交时会按回滚处理 |
| `noRollbackFor` | `Class<? extends Throwable>[]` | `{}` | 指定异常类型不触发回滚 |
| `noRollbackForClassName` | `String[]` | `{}` | 按异常类全名指定不触发回滚 |

## 深入阅读

- [传播行为](./propagation)：判断什么时候用 `REQUIRED`、`REQUIRES_NEW`、`NESTED`。
- [隔离级别](./isolation)：判断什么时候需要显式指定隔离级别。
- [事务管理器](./manager)：了解注解背后如何开启、提交和回滚事务。
