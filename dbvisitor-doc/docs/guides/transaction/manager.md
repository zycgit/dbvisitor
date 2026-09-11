---
id: manager
sidebar_position: 4
title: 10.4 事务管理器
description: 理解 TransactionManager、TransactionStatus、事务栈、挂起事务和保存点。
---

<span id="事务管理器" />

# 10.4 事务管理器

`TransactionManager` 是 dbVisitor 事务能力的核心接口。注解式事务和模板事务最终都会落到它的 `begin`、`commit`、`rollBack` 上。

## 接口能力

| 方法 | 作用 |
|---|---|
| `begin()` | 开启事务，默认 `Propagation.REQUIRED` + `Isolation.DEFAULT` |
| `begin(Propagation)` | 指定传播行为，使用默认隔离级别 |
| `begin(Propagation, Isolation)` | 指定传播行为和隔离级别 |
| `commit()` | 提交最近一次 `begin` 的事务 |
| `commit(TransactionStatus)` | 提交指定事务状态 |
| `rollBack()` | 回滚最近一次 `begin` 的事务 |
| `rollBack(TransactionStatus)` | 回滚指定事务状态 |
| `hasTransaction()` | 当前管理器是否还有未完成事务 |
| `isTopTransaction(TransactionStatus)` | 指定事务是否位于事务栈顶 |

一次 `begin(...)` 会返回一个 `TransactionStatus`，它表示这次事务开启后的状态：

| 方法 | 含义 |
|---|---|
| `getPropagation()` | 本次事务使用的传播行为 |
| `getIsolationLevel()` | 本次事务使用的隔离级别 |
| `isCompleted()` | 是否已经提交或回滚 |
| `isRollbackOnly()` | 是否被标记为回滚 |
| `isReadOnly()` | 是否被标记为只读 |
| `isNewConnection()` | 是否开启了新的数据库连接 |
| `isSuspend()` | 是否挂起过已有事务 |
| `hasSavepoint()` | 是否创建了保存点 |
| `setRollback()` | 标记提交时改为回滚 |
| `setReadOnly()` | 标记只读；提交时按回滚处理 |

## 本地事务管理器

默认实现是 `LocalTransactionManager`：

```java title='创建本地事务管理器'
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

TransactionManager txManager = new LocalTransactionManager(dataSource);
```

更常见的写法是通过 `TransactionHelper` 获取：

```java title='同一个 DataSource 复用同一个事务管理器'
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

`LocalTransactionManager` 绑定一个 `DataSource`，并通过线程上下文复用当前事务连接。事务中的 `JdbcTemplate`、Mapper 等访问同一个 `DataSource` 时，会拿到当前线程绑定的连接。

## 事务栈

同一个事务管理器可以连续 `begin` 多次。每次 `begin` 都会产生一个 `TransactionStatus`，并压入事务栈。

```java title='连续开启三个事务'
TransactionStatus tranA = txManager.begin();
TransactionStatus tranB = txManager.begin();
TransactionStatus tranC = txManager.begin();
```

栈结构可以理解为：

```text
栈顶
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

正常情况下应该按相反顺序结束：

```java
txManager.commit(tranC);
txManager.commit(tranB);
txManager.commit(tranA);
```

如果直接提交 `tranA`，dbVisitor 会先处理 `tranA` 之后开启的事务，再处理 `tranA`：

```java title='跨层提交'
txManager.commit(tranA);
```

等价于：

```java
txManager.commit(tranC);
txManager.commit(tranB);
txManager.commit(tranA);
```

这能避免事务栈残留，但业务代码仍建议显式按栈顶顺序提交或回滚，可读性更好。

## 传播行为如何影响连接

`begin(Propagation, Isolation)` 会根据当前线程是否已有事务，决定是否开启新连接、复用连接、挂起连接或创建保存点。

| 传播行为 | 已有事务时 | 连接/保存点效果 |
|---|---|---|
| `REQUIRED` | 加入已有事务 | 复用当前连接，不真正提交内层事务 |
| `REQUIRES_NEW` | 挂起已有事务，开启新事务 | 新建连接；结束后恢复外层连接 |
| `NESTED` | 创建嵌套事务 | 在当前连接上创建 Savepoint |
| `SUPPORTS` | 加入已有事务 | 复用当前连接 |
| `NOT_SUPPORTED` | 挂起已有事务，非事务执行 | 临时清除事务连接 |
| `NEVER` | 抛出异常 | 不允许在事务中运行 |
| `MANDATORY` | 加入已有事务 | 没有事务时抛出异常 |

## 提交、回滚和只读标记

`TransactionTemplateManager` 和 `TransactionInterceptor` 都遵循同一个规则：如果 `TransactionStatus` 被标记为回滚或只读，最后调用 `commit(...)` 时会实际执行回滚。

```java title='标记回滚后调用 commit'
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

如果 `quantity <= 0`，`commit(tran)` 会走回滚逻辑。

## 和三种使用方式的关系

```text
@Transactional
  -> TransactionInterceptor
      -> TransactionManager.begin(...)
      -> TransactionManager.commit(...) 或回滚

TransactionTemplate.execute(...)
  -> TransactionTemplateManager
      -> TransactionManager.begin(...)
      -> TransactionManager.commit(...) 或回滚

编程式事务
  -> 业务代码直接调用 TransactionManager
```

日常使用优先读 [注解式事务](./annotation) 和 [模板事务](./template)。只有在需要理解连接挂起、保存点、事务栈顺序时，再回到本页。
