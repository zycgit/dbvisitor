---
id: template
sidebar_position: 2
hide_table_of_contents: true
title: 模版事务
description: 了解 dbVisitor 中模版事务的用法。
---

# 模版事务

模版事务会遵循下面这个通用逻辑，自动处理提交和回滚：

```java
try {
    txManager.begin(propagation, isolation);
    ...
    txManager.commit();
} catch (Throwable e) {
    txManager.rollBack();
    throw e;
}
```

使用模版事务需要先创建 `TransactionTemplate`：

```java title='创建 TransactionTemplate'
DataSource dataSource = ...;
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate template = new TransactionTemplateManager(txManager);
```

然后通过 `execute` 方法执行事务代码块：

```java title='基本用法'
Object result = template.execute(tranStatus -> {
    // 在事务中执行业务逻辑
    return ...;
});
```

```java title='不需要返回值时'
template.execute((TransactionCallbackWithoutResult) tranStatus -> {
    // 执行事务操作
    ...
});
```

`execute` 方法还可以指定传播行为和隔离级别：

```java title='指定传播行为和隔离级别'
Object result = template.execute(tranStatus -> {
    return ...;
}, Propagation.REQUIRES_NEW, Isolation.READ_COMMITTED);
```

## 事务回滚

在模版事务中回滚有两种方式：
- **方式 1**：抛出一个异常，模版会自动回滚并将异常抛出。
- **方式 2**：通过 `setRollback()` 或 `setReadOnly()` 设置事务状态为回滚，该方式不会抛出异常。

```java title='通过状态标记回滚（不抛出异常）'
Object result = template.execute(tranStatus -> {
    tranStatus.setRollback();
    // 或
    tranStatus.setReadOnly();
    return ...;
});
```

## 获取 TransactionTemplate {#get-template}

```java title='通过 DataSource 创建'
DataSource dataSource = ...;
TransactionManager txManager = TransactionHelper.txManager(dataSource);
TransactionTemplate template = new TransactionTemplateManager(txManager);
```

```java title='通过依赖注入获取 TransactionManager 后创建'
public class TxExample {
    // @Inject                 < Guice、Solon 和 Hasor
    // @Resource or @Autowired < Spring
    private TransactionManager txManager;

    public void doWork() throws Throwable {
        TransactionTemplate template = new TransactionTemplateManager(txManager);
        template.execute(tranStatus -> {
            ...
            return null;
        });
    }
}
```

:::info[依赖注入方式使用请参考对应的文档]
- 基于 Spring 技术，[点击查看](../../yourproject/with_spring#tran)
- 基于 Solon 技术，[点击查看](../../yourproject/with_solon#tran)
- 在基于 Hasor 和 Guice 技术中可以使用 @Inject 进行注入
    - [查看 Guice 可注入的类型](../../yourproject/with_guice#inject)、 [查看 Hasor 可注入的类型](../../yourproject/with_hasor#inject)
:::
