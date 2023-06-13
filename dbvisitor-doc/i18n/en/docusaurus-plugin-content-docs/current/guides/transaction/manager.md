---
sidebar_position: 2
title: 事务管理器
description: dbVisitor ORM 事务管理器的使用。
---

# 事务管理器

事务管理器是在一个抽象的层面提供主要提供了如下几个重要的方法。

| 方法 | 作用 |
| ---- | ---- |
| begin() | 开启事务 |
| commit()、commit(TransactionStatus) | 递交事务 |
| rollBack()、rollBack(TransactionStatus) | 回滚事务 |
| hasTransaction() | 是否存在处理中的事务 |

:::tip
同一个事务管理器可以连续开启多个事务，不同事务之间的影响请参考 **[事务传播行为](./propagation.mdx)**
:::

## 事务栈

每次调用事务管理器的 `begin` 方法时就会产生一个新的事务、每产生一个事务就会压入一个叫做 **事务栈** 的数据结构中。
例如下面代码，通过 `TransactionManager` 开启了三个事务。

```java
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();
```

这三个事务在事务栈上的顺序如下表示，这是一个标准的 **顺序栈** 结构：

```text
+--------+--------+--------+ <<<< 入栈
| Tran A | Tran B | Tran C |  
+--------+--------+--------+ >>>> 出栈
```

通常情况下针对事务的操作都是按照顺序的，这在事务栈上就犹如 **入栈** 和 **出栈** 的操作，例如：

```java
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();
...
manager.commit(tranC);
manager.commit(tranB);
manager.commit(tranA);
```

dbVisitor 有了 **事务栈** 之后允许操作任意位置的事务，例如：在连续开启了三个事务之后一次性递交三个事务。

```java
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();
...
manager.commit(tranA);
```

像这种跨越顺序操作事务，dbVisitor 会自动将 C 和 B 两个事务按照它们的顺序进行 `commit` 最后在 `commit` 它自己。
因此也等价于下面这个方法：

```java
manager.commit(tranC);
manager.commit(tranB);
manager.commit(tranA);
```

## 事务管理器

以 `DataSourceManager.getManager(dataSource);` 方式得到的事务管理器就是本地事务管理器。

本地事务管理器的特点是 **数据源的事务管理器与当前线程形成一对一绑定**，基于这个绑定关系可以达到 **[本地同步](./datasource.md#本地同步)** 的目的

dbVisitor 提供了三种方式使用事务，分别为：

- **声明式事务**，通过调用 `TransactionManager` 接口来实现事务控制。
- **模版事务**，通过 `TransactionTemplate` 接口来实现事务控制。
- **注解事务**，基于 `@Transaction` 的注解事务控制（开发中...）

### 声明式事务

启动和递交一个事务，例如：

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();

...

manager.commit(tranA);
```

或者使用快捷方式

```java {4,8}
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

manager.begin(); // 开启一个事务

...

manager.commit(); //递交最近的一个事务
```

启动和递交多个事务，例如：

```java
DataSource dataSource = DsUtils.dsMySql();
TransactionManager manager = DataSourceManager.getManager(dataSource);

TransactionStatus tranA = manager.begin();
TransactionStatus tranB = manager.begin();
TransactionStatus tranC = manager.begin();

...

manager.commit(tranC);
manager.commit(tranB);
manager.commit(tranA);
```

通过 `begin` 方法的参数可以设置事务的 **[传播属性](./propagation.mdx)** 和 **[隔离级别](./Isolation.md)**

```java
TransactionStatus tranA = manager.begin(
        Propagation.REQUIRES_NEW, // 传播属性
        Isolation.READ_COMMITTED  // 隔离级别
);
```

### 模版事务

通常使用事务都会遵循下列逻辑：

```java {2,6,8}
try {
    manager.begin(behavior, level);

    ...

    manager.commit();
} catch (Throwable e) {
    manager.rollBack();
    throw e;
}
```

而模版事务会遵循这个常规逻辑使其变为一个更加通用 API 调用方式，下面这段代码就是模版事务类的实现逻辑：

```java {5,9,14} title="类：net.hasor.dbvisitor.transaction.TransactionTemplateManager"
public <T> T execute(TransactionCallback<T> callBack, 
                     Propagation behavior, Isolation level) throws Throwable {
    TransactionStatus tranStatus = null;
    try {
        tranStatus = this.transactionManager.begin(behavior, level);
        return callBack.doTransaction(tranStatus);
    } catch (Throwable e) {
        if (tranStatus != null) {
            tranStatus.setRollback();
        }
        throw e;
    } finally {
        if (tranStatus != null && !tranStatus.isCompleted()) {
            this.transactionManager.commit(tranStatus);
        }
    }
}
```

使用模版事务的方式为：

```java
Object result = template.execute(new TransactionCallback<Object>() {
    @Override
    public Object doTransaction(TransactionStatus tranStatus) throws Throwable {
        ...
        return null;
    }
});

// 使用 Java8 Lambda 语法可以简化为下面这种
Object result = template.execute(tranStatus -> {
    return ...;
});
```

在事务模版中抛出异常会导致事务回滚，同时异常会继续上抛：

```java {4}
try {
    Object result = template.execute(new TransactionCallback<Object>() {
        public Object doTransaction(TransactionStatus tranStatus) throws Throwable {
            throw new Exception("...");
        }
    });
} catch (Throwable e) {
    ... run here
}
```

也可以设置事务状态为 `rollBack` 或 `readOnly` 也会导致回滚

```java {3,5}
Object result = template.execute(new TransactionCallback<Object>() {
    public Object doTransaction(TransactionStatus tranStatus) throws Throwable {
        tranStatus.setReadOnly();
        // 或
        tranStatus.setRollback();

        return ...;
    }
});
```

没有返回值的模版事务，需要用到 `TransactionCallbackWithoutResult` 接口。具体用法如下：

```java
template.execute((TransactionCallbackWithoutResult) tranStatus -> {
    ...
});
```
