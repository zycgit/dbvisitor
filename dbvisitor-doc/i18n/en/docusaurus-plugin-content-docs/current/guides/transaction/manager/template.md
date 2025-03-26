---
id: template
sidebar_position: 2
hide_table_of_contents: true
title: 模版事务
description: 了解 dbVisitor 中模版事务的用法。
---

模版事务会遵循下面这个通用逻辑：

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

使用模版事务代码如下：

```java
TransactionTemplate template = ...
Object result = template.execute(new TransactionCallback<Object>() {
    @Override
    public Object doTransaction(TransactionStatus tranStatus) throws Throwable {
        ...
        return null;
    }
});
```

```java title='Java8 语法简化'
Object result = template.execute(tranStatus -> {
    return ...;
});
```

```java title='例：代码块在事务中执行，并忽略返回结果'
template.execute((TransactionCallbackWithoutResult) tranStatus -> {
    ...
});
```

## 事务回滚

在模版事务中事务回滚有两种方式：
- 方式1：抛出一个异常。
- 方式2：通过 `rollBack` 或 `readOnly` 方法设置事务状态为回滚，该方式不会抛出异常。

```java title='回滚事务不抛出异常'
Object result = template.execute(new TransactionCallback<Object>() {
    public Object doTransaction(TransactionStatus tranStatus) {
        tranStatus.setReadOnly();
        // 或
        tranStatus.setRollback();

        return ...;
    }
});
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
