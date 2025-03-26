---
id: annotation
sidebar_position: 3
hide_table_of_contents: true
title: 注解式事务
description: 了解 dbVisitor 中注解事务的用法。
---

:::info[重要提示]
由于需要借助拦截器才能正常工作，因此和不同的框架技术一起使用时可能涉及不同的注解。例如：
- 使用 Spring 的事务注解进行事务控制，[点击查看](../../yourproject/with_spring#tran)
- 使用 Solon 的事务注解进行事务控制，[点击查看](../../yourproject/with_solon#tran)
:::


```java title='在基于 Hasor 和 Guice 技术中可以使用如下方式：'
import net.hasor.dbvisitor.transaction.Transactional;

public class TxExample {
    @Transactional(propagation = Propagation.REQUIRES)
    public void exampleMethod() {
        ...
    }
}
```

## 原始应用

原始应用是指没有借助任何字节码增强技术的应用程序，或者在不具备字节码增强的情况中使用注解事务。

```java title='对象增强'
DataSource dataSource = ...
TxExample txExample = ...

// 增强 txExample 对象
//   - 为 txExample 对象生成一个代理对象，代理对象会在原始对象上提供拦截器。
txExample = TransactionHelper.support(txExample, dataSource);

txExample.exampleMethod(); // 代理对象会处理 @Transactional 注解
```
