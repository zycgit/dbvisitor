---
id: annotation
sidebar_position: 3
hide_table_of_contents: true
title: 注解式事务
description: 了解 dbVisitor 中 @Transactional 注解事务的用法。
---

# 注解式事务

通过 `@Transactional` 注解标记方法或类，声明式地控制事务。

```java title='基本用法'
import net.hasor.dbvisitor.transaction.Transactional;

public class TxExample {
    @Transactional
    public void exampleMethod() {
        ...
    }
}
```

## 注解属性 {#attrs}

`@Transactional` 支持以下属性：

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `propagation` | `Propagation` | `REQUIRED` | 事务传播行为 |
| `isolation` | `Isolation` | `DEFAULT` | 事务隔离级别 |
| `readOnly` | `boolean` | `false` | 是否为只读事务 |
| `noRollbackFor` | `Class<? extends Throwable>[]` | `{}` | 遇到指定异常不回滚 |
| `noRollbackForClassName` | `String[]` | `{}` | 按类名匹配不回滚的异常 |

```java title='指定传播行为和隔离级别'
@Transactional(propagation = Propagation.REQUIRED,
               isolation = Isolation.READ_COMMITTED)
public void exampleMethod() {
    ...
}
```

```java title='指定不回滚的异常'
@Transactional(noRollbackFor = { IllegalArgumentException.class })
public void exampleMethod() {
    ...
}
```

## 启用注解事务

`@Transactional` 注解需要借助拦截器才能生效。根据使用的框架技术不同，有以下方式：

:::info[与框架集成]
- 使用 Spring 事务注解，[点击查看](../../yourproject/with_spring#tran)
- 使用 Solon 事务注解，[点击查看](../../yourproject/with_solon#tran)
:::

### 原始应用 {#raw}

在没有框架支持的情况下，可通过 `TransactionHelper.support()` 手动创建代理对象来启用注解事务。

```java title='通过代理对象启用注解事务'
DataSource dataSource = ...;
TxExample txExample = new TxExample();

// 为 txExample 生成一个代理对象，代理会自动处理 @Transactional 注解
txExample = TransactionHelper.support(txExample, dataSource);

// 调用代理对象的方法时，事务将自动管理
txExample.exampleMethod();
```

:::tip[提示]
`TransactionHelper.support()` 支持可变参数 `DataSource...`，可以同时绑定多个数据源的事务拦截。
:::
