---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: 事务管理器
description: dbVisitor 事务管理器的工作原理。
---

`TransactionManager` 接口是 dbVisitor 事务管理的核心，它在抽象层面提供了如下方法：

| 方法 | 作用 |
|---|---|
| `begin()` | 开启事务（默认 `REQUIRED` + `DEFAULT`） |
| `begin(Propagation)` | 指定传播行为开启事务 |
| `begin(Propagation, Isolation)` | 指定传播行为和隔离级别开启事务 |
| `commit()` / `commit(TransactionStatus)` | 递交事务 |
| `rollBack()` / `rollBack(TransactionStatus)` | 回滚事务 |
| `hasTransaction()` | 是否存在处理中的事务 |
| `isTopTransaction(TransactionStatus)` | 指定事务是否位于栈顶 |

:::info
- 同一个事务管理器可以连续开启多个事务，不同事务之间的影响请参考 **[传播行为](../propagation)**、**[隔离级别](../isolation)**
- `TransactionManager` 实现了 `Closeable` 接口，可用于 try-with-resources。
:::

## 工作原理

每次调用事务管理器的 begin 方法时就会产生一个新的事务、每产生一个事务就会压入一个叫做 **事务栈** 的数据结构中。

```java title='例：通过 TransactionManager 开启了三个事务'
DataSource dataSource = ...
TransactionManager txManager = new LocalTransactionManager(dataSource);

TransactionStatus tranA = txManager.begin();
TransactionStatus tranB = txManager.begin();
TransactionStatus tranC = txManager.begin();
...
txManager.commit(tranC);
txManager.commit(tranB);
txManager.commit(tranA);
```

这三个事务在 **事务栈** 上的顺序如下表示，这是一个标准的 **顺序栈** 结构：

```text
+--------+--------+--------+ <<<< 入栈
| Tran A | Tran B | Tran C |  
+--------+--------+--------+ >>>> 出栈
```

:::info[提示]
dbVisitor 有了 **事务栈** 之后允许操作任意位置的事务，例如：在连续开启了三个事务之后一次性递交三个事务。

```java
DataSource dataSource = ...
TransactionManager manager = new LocalTransactionManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();
...
manager.commit(tranA);
```

跨越顺序操作事务 dbVisitor 会自动按照顺序进行操作，此时相当于如下操作：
```java
manager.commit(tranC);
manager.commit(tranB);
manager.commit(tranA);
```
:::

## 如何使用 {#useit}

dbVisitor 提供了三种方式使用事务，分别为：
- [编程式事务](./program)，通过调用 `LocalTransactionManager` 类方法来手动控制事务。
- [模版事务](./template)，通过 `TransactionTemplate` 接口执行事务代码块，自动处理提交/回滚。
- [注解事务](./annotation)，基于 `@Transactional` 注解声明式控制事务。