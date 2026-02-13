---
id: program
sidebar_position: 1
hide_table_of_contents: true
title: 编程式事务
description: 了解 dbVisitor 中编程式事务的用法。
---

# 编程式事务

编程式事务通过手动调用 `TransactionManager` 的方法来控制事务的开启、提交和回滚。

```java title='基本用法'
TransactionManager txManager = ...;

TransactionStatus tranA = txManager.begin();
try {
    // 执行业务逻辑
    ...
    txManager.commit(tranA);
} catch (Throwable e) {
    txManager.rollBack(tranA);
    throw e;
}
```

通过 `begin` 方法的参数可以设置事务的 [传播行为](../propagation) 和 [隔离级别](../isolation)：

```java title='指定传播行为和隔离级别'
TransactionStatus tranA = txManager.begin(
        Propagation.REQUIRES_NEW, // 传播行为
        Isolation.READ_COMMITTED  // 隔离级别
);
```

也可以只指定传播行为（隔离级别使用默认值）：

```java title='只指定传播行为'
TransactionStatus tranA = txManager.begin(Propagation.REQUIRES_NEW);
```

## 获取事务管理器

```java title='方式 1：通过 TransactionHelper（同一 DataSource 共享实例）'
DataSource dataSource = ...;
TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

```java title='方式 2：直接创建 LocalTransactionManager'
DataSource dataSource = ...;
TransactionManager txManager = new LocalTransactionManager(dataSource);
```

```java title='方式 3：通过依赖注入获取'
public class TxExample {
    // @Inject                 < Guice、Solon 和 Hasor
    // @Resource or @Autowired < Spring
    private TransactionManager txManager;
    ...
}
```

:::info[依赖注入方式使用请参考对应的文档]
- 基于 Spring 技术，[点击查看](../../yourproject/with_spring#tran)
- 基于 Solon 技术，[点击查看](../../yourproject/with_solon#tran)
- 在基于 Hasor 和 Guice 技术中可以使用 @Inject 进行注入
  - [查看 Guice 可注入的类型](../../yourproject/with_guice#inject)、 [查看 Hasor 可注入的类型](../../yourproject/with_hasor#inject)
:::
