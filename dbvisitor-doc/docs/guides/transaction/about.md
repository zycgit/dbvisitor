---
id: about
sidebar_position: 0
title: 数据库事务
description: dbVisitor 内置本地事务能力，支持注解式、模板式、编程式事务，以及传播行为和隔离级别控制。
---

# 数据库事务

dbVisitor 提供一套轻量的本地事务能力，用来把多次数据库操作放进同一个提交或回滚边界里。它适合处理“创建订单后扣库存”、“写主表后写明细”、“一段逻辑中部分操作需要独立提交”这类场景。

事务由 `TransactionManager` 管理，一个事务管理器绑定一个 `DataSource`。在事务中，dbVisitor 会把当前线程上的数据库连接交给 `JdbcTemplate`、Mapper、BaseMapper、LambdaTemplate 等 API 复用；事务结束时再统一提交或回滚。

:::info
dbVisitor 管理的是本地事务，不是分布式事务。多个 `DataSource` 可以分别绑定事务拦截器，但不同数据库之间不会自动具备两阶段提交能力。
:::

## 先选使用方式

| 使用方式 | 适合 | 不适合 | 入口 |
|---|---|---|---|
| 注解式事务 | Service 方法边界清楚，希望业务代码最干净 | 没有代理/拦截器，或者事务边界需要动态决定 | [注解式事务](./annotation) |
| 模板事务 | 只有局部代码需要事务，希望自动处理提交和回滚 | 需要手动控制多个事务状态的提交顺序 | [模板事务](./template) |
| 编程式事务 | 需要完全控制 `begin`、`commit`、`rollBack` | 常规业务方法，容易写出重复 try/catch | [编程式事务](./program) |

一般建议：
- 在 Spring、Solon、Guice、Hasor 这类带 DI/代理能力的项目里，优先用注解式事务。
- 在普通 Java 程序、脚本式任务、局部事务代码里，优先用模板事务。
- 只有确实需要手动控制事务状态时，再使用编程式事务。

## 一个完整效果

下面的例子希望达成一个效果：创建订单和订单明细必须同时成功；任意一步失败，都要一起回滚。

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

如果 `order_item` 插入成功后，扣库存 SQL 抛出异常，事务拦截器会把当前事务标记为回滚，最后执行回滚。最终效果是：订单、明细、库存都不会留下半成品数据。

## 代码结构

事务相关代码都在 `net.hasor.dbvisitor.transaction` 包下：

| 类型 | 作用 | 常见使用位置 |
|---|---|---|
| `@Transactional` | 声明方法或类需要事务 | Service 方法、业务入口方法 |
| `Propagation` | 控制当前方法如何加入、创建、挂起事务 | 注解、模板、编程式 `begin` |
| `Isolation` | 控制数据库并发读写的隔离级别 | 注解、模板、编程式 `begin` |
| `TransactionManager` | 事务管理核心接口，提供 `begin/commit/rollBack` | 编程式事务、模板内部 |
| `TransactionStatus` | 一次事务开启后的状态对象，可标记回滚、只读 | 模板回调、编程式事务 |
| `TransactionTemplate` | 用回调包装事务边界 | 局部事务代码 |
| `TransactionTemplateManager` | `TransactionTemplate` 的默认实现 | 手动创建模板事务 |
| `TransactionHelper` | 从 `DataSource` 获取事务管理器，或创建注解代理 | 普通 Java 程序 |
| `LocalTransactionManager` | 本地事务管理器实现，一个实例绑定一个 `DataSource` | 直接创建或由 `TransactionHelper` 创建 |

内部结构可以理解为：

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

## 深入阅读

- [注解式事务](./annotation)：最常用的事务写法。
- [模板事务](./template)：在一段代码块里使用事务。
- [编程式事务](./program)：手动控制事务生命周期。
- [事务管理器](./manager)：理解 `TransactionManager`、事务栈、挂起和保存点。
- [传播行为](./propagation)：理解 `REQUIRED`、`REQUIRES_NEW`、`NESTED` 等行为。
- [隔离级别](./isolation)：理解脏读、不可重复读、幻读和数据库隔离级别。
