---
id: template
sidebar_position: 2
title: 10.2 模板事务
description: 使用 TransactionTemplate 在代码块内自动提交或回滚事务。
---

# 模板事务

模板事务适合在一段局部代码里使用事务。它把固定的事务控制逻辑包起来：先 `begin`，正常结束就 `commit`，抛出异常或标记回滚时就回滚。

## 适合

- 普通 Java 程序中只想包住一段代码。
- 不想为 `@Transactional` 准备代理对象。
- 希望避免重复写 try/catch/finally。

## 不适合

- 事务边界就是 Service 方法，且项目已有代理能力。
- 需要手动保留多个 `TransactionStatus`，并按指定顺序提交或回滚。

## 基本用法

```java title='创建 TransactionTemplate'
import javax.sql.DataSource;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

DataSource dataSource = ...;
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate txTemplate = new TransactionTemplateManager(txManager);
```

```java title='在事务中返回结果'
Long orderId = txTemplate.execute(tranStatus -> {
    Long newOrderId = jdbcTemplate.queryForObject(
            "select next value for seq_order",
            Long.class
    );

    jdbcTemplate.executeUpdate(
            "insert into orders(id, user_id) values(?, ?)",
            newOrderId, userId
    );
    jdbcTemplate.executeUpdate(
            "insert into order_item(order_id, sku_id) values(?, ?)",
            newOrderId, skuId
    );
    return newOrderId;
});
```

执行效果：
- 回调正常返回时，模板提交事务，并把 `newOrderId` 作为 `execute` 的返回值。
- 回调抛出异常时，模板把事务标记为回滚，然后重新抛出异常。

## 无返回值代码块

```java title='使用 TransactionCallbackWithoutResult'
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

## 主动标记回滚

有些场景不想抛异常，但希望当前事务最终回滚，可以在回调里调用 `tranStatus.setRollback()`。

```java title='校验失败时回滚，但返回业务结果'
Boolean success = txTemplate.execute(tranStatus -> {
    int updated = jdbcTemplate.executeUpdate(
            "update sku_stock set quantity = quantity - ? where sku_id = ? and quantity >= ?",
            quantity, skuId, quantity
    );

    if (updated == 0) {
        tranStatus.setRollback();
        return false;
    }

    jdbcTemplate.executeUpdate(
            "insert into stock_log(sku_id, quantity) values(?, ?)",
            skuId, quantity
    );
    return true;
});
```

执行效果：
- 库存充足时，扣库存和日志一起提交，返回 `true`。
- 库存不足时，不抛异常，但事务回滚，返回 `false`。

## 指定传播行为和隔离级别

```java title='用独立事务写审计日志'
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;

txTemplate.execute(tranStatus -> {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            orderId, "CREATE"
    );
    return null;
}, Propagation.REQUIRES_NEW, Isolation.READ_COMMITTED);
```

如果这段代码在外层事务里执行，`REQUIRES_NEW` 会挂起外层事务，打开新的连接执行审计日志事务。日志提交或回滚完成后，再恢复外层事务。

## 获取 TransactionTemplate

```java title='普通 Java 程序'
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate txTemplate = new TransactionTemplateManager(txManager);
```

```java title='依赖注入环境'
public class OrderService {
    // @Inject                 < Guice、Solon 和 Hasor
    // @Resource or @Autowired < Spring
    private TransactionTemplate txTemplate;
}
```

相关集成：
- [Spring 集成](../yourproject/with_spring#tran)
- [Solon 集成](../yourproject/with_solon#tran)
- [Guice 可注入类型](../yourproject/with_guice#inject)
- [Hasor 可注入类型](../yourproject/with_hasor#inject)

## 深入阅读

- [事务管理器](./manager)：模板内部如何调用 `TransactionManager`。
- [传播行为](./propagation)：模板的第二个参数应该怎么选。
- [隔离级别](./isolation)：模板的第三个参数应该怎么选。
