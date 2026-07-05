---
id: propagation
sidebar_position: 5
title: 10.5 传播行为
description: dbVisitor 事务传播行为详解。
---

# 事务传播行为

当多个事务方法在同一线程中相互调用时，传播行为决定了事务如何在这些方法间传播。

传播行为可以用于三种事务入口：

```java title='注解式事务'
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void writeAuditLog(long orderId) {
    ...
}
```

```java title='模板事务'
txTemplate.execute(tranStatus -> {
    ...
    return null;
}, Propagation.NESTED);
```

```java title='编程式事务'
TransactionStatus tran = txManager.begin(Propagation.REQUIRED);
```

## 如何选择

| 想达到的效果 | 推荐传播行为 |
|---|---|
| 大多数业务方法：有事务就加入，没有就新建 | `REQUIRED` |
| 审计日志、操作记录：希望独立提交 | `REQUIRES_NEW` |
| 外层事务中某一步可以失败，但不影响外层继续 | `NESTED` |
| 有事务就跟随，没有事务也能运行 | `SUPPORTS` |
| 一段代码必须绕开事务执行 | `NOT_SUPPORTED` |
| 明确禁止在事务中运行 | `NEVER` |
| 必须由外层事务包住，否则就是调用错误 | `MANDATORY` |

## 加入已有事务 (REQUIRED)

尝试加入已经存在的事务中，如果没有则开启一个新的事务。
- 常量 `Propagation.REQUIRED`

```java title='订单创建默认使用 REQUIRED'
@Transactional
public void createOrder(long orderId) {
    orderMapper.insertOrder(orderId);
    orderMapper.insertOrderItems(orderId);
}
```

`REQUIRED` 是默认值。外层没有事务时，它创建事务；外层已经有事务时，它加入外层事务。内层方法正常返回并不代表立即提交，最终提交或回滚由最外层事务决定。

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 加入事务 A（不做操作） |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 不做操作（由外层决定） |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 独立事务 (REQUIRES_NEW)

挂起当前已存在的事务（如果有），开启一个全新的独立事务，新事务与旧事务彼此无关。
- 常量 `Propagation.REQUIRES_NEW`

```java title='外层失败时，审计日志仍可独立提交'
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void writeAuditLog(long orderId, String action) {
    jdbcTemplate.executeUpdate(
            "insert into order_audit(order_id, action) values(?, ?)",
            orderId, action
    );
}
```

:::info
- 挂起会导致当前线程绑定的 Connection 暂时不可用。
- 挂起后事务管理器会创建一个新的 Connection 用作当前线程的数据库连接。
:::

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 挂起 A → 新建 Connection B → 开启事务 B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 提交/回滚 B → 恢复 A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 嵌套事务 (NESTED)

在当前事务中通过 `Savepoint` 方式开启一个子事务。子事务回滚不影响外层事务，但外层事务回滚会连带子事务。
- 常量 `Propagation.NESTED`

```java title='优惠券失败时，只回滚优惠券部分'
txTemplate.execute(tranStatus -> {
    couponMapper.bindCoupon(orderId, couponId);
    return null;
}, Propagation.NESTED);
```

`NESTED` 依赖数据库和驱动对 Savepoint 的支持。它适合“一个大事务中的局部步骤允许失败”的场景。

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 创建 Savepoint B |
| T4 | | insert data2 | |
| T5 | | commit/rollback | 释放/回滚 Savepoint B |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 跟随环境 (SUPPORTS)

如果当前没有事务，以非事务方式执行；如果有事务，则加入当前事务（效果等同 REQUIRED）。
- 常量 `Propagation.SUPPORTS`

```java title='查询方法可以跟随调用方环境'
@Transactional(propagation = Propagation.SUPPORTS)
public OrderInfo queryOrder(long orderId) {
    return orderMapper.queryOrder(orderId);
}
```

:::info
SUPPORTS 的本质是**不作为** — 不会主动开启也不会阻止事务。
:::

## 非事务方式 (NOT_SUPPORTED)

如果当前没有事务，以非事务方式执行；如果有事务，则将当前事务**挂起**后以非事务方式执行。
- 常量 `Propagation.NOT_SUPPORTED`

```java title='大查询不占用外层事务'
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public List<OrderReport> queryReport() {
    return reportMapper.queryOrderReport();
}
```

| 时序 | 事务 A | 事务 B | 效果 |
|---|---|---|---|
| T1 | begin | | 开启事务 A |
| T2 | insert data1 | | |
| T3 | | begin | 挂起事务 A |
| T4 | | insert data2 | 非事务方式执行 |
| T5 | | commit/rollback | 恢复事务 A |
| T6 | insert data3 | | |
| T7 | commit/rollback | | 提交/回滚事务 A |

## 排除事务 (NEVER)

如果当前没有事务，以非事务方式执行；如果有事务，直接**抛出异常**。
- 常量 `Propagation.NEVER`

```java title='禁止事务内调用'
@Transactional(propagation = Propagation.NEVER)
public void rebuildSearchIndex() {
    ...
}
```

## 要求事务 (MANDATORY)

如果当前有事务，加入当前事务；如果没有事务，直接**抛出异常**。
- 常量 `Propagation.MANDATORY`

```java title='必须由外层事务保护'
@Transactional(propagation = Propagation.MANDATORY)
public void insertOrderItem(long orderId, long skuId) {
    orderMapper.insertOrderItem(orderId, skuId);
}
```

## 传播行为对比

| 传播行为 | 无事务时 | 有事务时 |
|---|---|---|
| `REQUIRED` | 新建事务 | 加入已有事务 |
| `REQUIRES_NEW` | 新建事务 | 挂起已有 → 新建事务 |
| `NESTED` | 新建事务 | Savepoint 子事务 |
| `SUPPORTS` | 非事务执行 | 加入已有事务 |
| `NOT_SUPPORTED` | 非事务执行 | 挂起已有 → 非事务执行 |
| `NEVER` | 非事务执行 | 抛出异常 |
| `MANDATORY` | 抛出异常 | 加入已有事务 |

## 深入阅读

- [注解式事务](./annotation)：在 `@Transactional` 上指定传播行为。
- [模板事务](./template)：在 `execute` 的第二个参数中指定传播行为。
- [事务管理器](./manager)：理解 `REQUIRES_NEW` 的连接挂起和 `NESTED` 的保存点。
