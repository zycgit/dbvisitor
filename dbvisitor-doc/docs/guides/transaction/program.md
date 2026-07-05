---
id: program
sidebar_position: 3
title: 10.3 编程式事务
description: 直接使用 TransactionManager 手动控制事务的开启、提交和回滚。
---

# 编程式事务

编程式事务直接使用 `TransactionManager`。它最灵活，也最容易写错，适合需要精确控制事务状态的场景。

## 适合

- 需要手动保留 `TransactionStatus`。
- 一个方法里要显式开启多个事务，并控制它们的提交或回滚。
- 想直接观察 `hasTransaction()`、`isTopTransaction(...)` 等事务状态。

## 不适合

- 常规 Service 方法事务，优先用 [注解式事务](./annotation)。
- 只想包住一段代码，优先用 [模板事务](./template)。

## 基本用法

```java title='手动提交和回滚'
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

TransactionManager txManager = TransactionHelper.txManager(dataSource);

TransactionStatus tran = txManager.begin();
try {
    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            orderId, userId
    );
    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            orderId, skuId
    );

    txManager.commit(tran);
} catch (Throwable e) {
    txManager.rollBack(tran);
    throw e;
}
```

执行效果：
- 两个 SQL 都成功，`commit(tran)` 提交事务。
- 任意 SQL 抛出异常，`rollBack(tran)` 回滚事务。

## 指定传播行为和隔离级别

```java title='开启独立事务'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;

TransactionStatus auditTran = txManager.begin(
        Propagation.REQUIRES_NEW,
        Isolation.READ_COMMITTED
);
try {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            orderId, "CREATE"
    );
    txManager.commit(auditTran);
} catch (Throwable e) {
    txManager.rollBack(auditTran);
    throw e;
}
```

`REQUIRES_NEW` 会在当前线程已经有事务时挂起旧事务，并创建一个新连接执行当前事务。

## 只回滚内层嵌套事务

`NESTED` 会在已有事务中创建保存点。内层回滚只回滚到保存点，不会直接结束外层事务。

```java title='使用 NESTED 回滚一段局部操作'
TransactionStatus outer = txManager.begin();
try {
    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            orderId, userId
    );

    TransactionStatus nested = txManager.begin(Propagation.NESTED);
    try {
        jdbcTemplate.executeUpdate(
                "insert into order_coupon(order_id, coupon_id) values(?, ?)",
                orderId, couponId
        );
        txManager.commit(nested);
    } catch (Throwable couponError) {
        txManager.rollBack(nested);
    }

    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            orderId, skuId
    );
    txManager.commit(outer);
} catch (Throwable e) {
    txManager.rollBack(outer);
    throw e;
}
```

执行效果：
- 优惠券写入失败时，只回滚 `order_coupon` 对应的保存点。
- 外层订单和明细仍然可以继续提交。
- 如果外层最终回滚，内层已经成功的保存点也会随外层一起回滚。

## 获取 TransactionManager

```java title='方式 1：通过 TransactionHelper 获取'
TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

`TransactionHelper.txManager(dataSource)` 会为同一个 `DataSource` 复用同一个事务管理器实例。

```java title='方式 2：直接创建 LocalTransactionManager'
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

TransactionManager txManager = new LocalTransactionManager(dataSource);
```

```java title='方式 3：依赖注入'
public class OrderService {
    // @Inject                 < Guice、Solon 和 Hasor
    // @Resource or @Autowired < Spring
    private TransactionManager txManager;
}
```

## 注意事项

- `commit(...)` 和 `rollBack(...)` 只能对未完成的事务调用一次。
- 如果提交或回滚的事务不在栈顶，dbVisitor 会先处理它之后开启的事务。
- `TransactionManager` 实现了 `Closeable`，但常规代码中更推荐显式 `commit` 或 `rollBack`，让事务边界一眼可见。

## 深入阅读

- [事务管理器](./manager)：了解事务栈如何影响提交和回滚顺序。
- [传播行为](./propagation)：了解每个 `Propagation` 对连接、保存点、挂起的影响。
- [隔离级别](./isolation)：了解 `Isolation` 如何映射到 JDBC 连接隔离级别。
