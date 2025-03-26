---
id: program
sidebar_position: 1
hide_table_of_contents: true
title: 编程式事务
description: 了解 dbVisitor 中编程式事务的用法。
---

编程式事务是指事务操作的需要通过编写代码方式来实现。

```java title='基本样例'
TransactionManager txManager =  ...

// begin
TransactionStatus tranA = txManager.begin();
...
// commint
txManager.commit(tranA);
```

通过 begin 方法的参数可以设置事务的 [传播属性](../propagation) 和 [隔离级别](../isolation)

```java title='指定传播属性和隔离级别'
TransactionStatus tranA = txManager.begin(
        Propagation.REQUIRES_NEW, // 传播属性
        Isolation.READ_COMMITTED  // 隔离级别
);
```

## 获取事务管理器

```java title='通过 DataSource 获取'
DataSource dataSource = ...
TransactionManager txManager = TransactionHelper.txManager(dataSource);
```

```java title='通过 依赖注入 获取'
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
