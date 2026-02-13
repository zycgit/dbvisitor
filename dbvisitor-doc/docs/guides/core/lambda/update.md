---
id: update
sidebar_position: 3
hide_table_of_contents: true
title: 更新操作
description: 在 dbVisitor 中使用 LambdaTemplate 更新数据有三种用法。
---

# 更新操作

在 dbVisitor 中使用 LambdaTemplate 更新数据有三种用法，在使用过程中可以灵活搭配以满足需要。具体如下：
- [字段更新](./update#field)，更新某个或者某些特定的字段。
- [参照样本](./update#sample)，在更新多个字段时参考样本对象中的数据。
- [整行覆盖](./update#overwrite)，这种方式将会使用新数据覆盖整行数据。

:::info[提示]
更新操作中涉及查询条件相关内容请参考 **[条件构造器](./where-builder)**。
:::

## 字段更新 {#field}

```java title='基本用法 1：使用 Lambda 表达式'
LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .eq(User::getId, 1)              // 匹配条件
                   .updateTo(User::getName, "Mary") // 更新字段，使用 Lambda
                   .updateTo(User::getStatus, 2)    // 可通过链式调用更新多个字段
                   .doUpdate();
// 返回 result 为受影响的行数
```

```java title='基本用法 2：使用 字符串属性'
LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .eq(User::getId, 1)               // 匹配条件
                   .updateToUsingStr("name", "Mary") // 更新字段，通过字符串指定属性
                   .updateToUsingStr("status", 2)    // 可通过链式调用更新多个字段
                   .doUpdate();
// 返回 result 为受影响的行数
```

## 参照样本 {#sample}

当有多个字段需要更新时，可以通过构造一个样本对象来简化 updateTo 或 updateToUsingStr 方法的调用链。例如：

```java title='使用 Bean 作为样本对象'
User sample = new User();
sample.setName("new name");
sample.setStatus(2);

LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .ne(User::getStatus, 2) // 匹配条件 status 不为 2 的记录
                   .updateToSample(sample) // 更新 name 和 status 两个属性
                   .doUpdate();
// 返回 result 为受影响的行数
```

```java title='使用 Map 作为样本对象'
Map<String, Object> sample = new HashMap<>();
sample.put("name", "new name");
sample.put("status", 2);

LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .ne(User::getStatus, 2)    // 匹配条件 status 不为 2 的记录
                   .updateToSampleMap(sample) // 更新 name 和 status 两个属性
                   .doUpdate();
// 返回 result 为受影响的行数
```

:::info[使用提示]
在使用样本时需要注意以下几点：
- updateToSample 方法只会寻找样本中不为空的属性作为更新字段，因此：
  - 不需要被更新的属性在样本对象中需要设置为 NULL。
  - 不建议在样本对象中使用 byte、short、int、long、float、double、char 基本类型属性。
  - 若是需要更新为 NULL，可以追加使用 [字段更新](./update#field)。
- 如果多次调用 updateToSample 设置样本数据，那么先后两次样本中重叠部分会被覆盖。
:::

## 整行覆盖 {#overwrite}

整行覆盖操作是比较危险的操作，它会将整条记录使用新的数据做替换。

```java title='使用 Bean 对象'
User user = new User();
user.setName("new name");
user.setStatus(2);

LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .ne(User::getStatus, 2) // 匹配条件 status 不为 2 的记录
                   .updateRow(user)        // 所有列都参照 user 对象的值进行更新
                   .doUpdate();
// 返回 result 为受影响的行数
```

例如上面例子中：
- User 对象总共有 `id`、`name`、`status`、`age` 4 个属性
- 更新操作设置了 2 个属性
- `id` 属性为主键。

最终执行更新时 `name`、`status`、`age` 三个属性会被设置，其中 age 属性会被设置 NULL。

## 主键列更新

:::info[重点]
在 dbVisitor 中除非明确指定否则主键列默认是不参与更新操作。受此规则影响：
- updateToSample、updateRow 两个方法默认情况下会忽略主键列的存在。
- 其它方法中如果出现更新主键列会引发异常
:::

通过 dbVisitor 更新主键列，需要调用 allowUpdateKey 方法来允许本次更新操作更新主键列。

```java
LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .eq(User::getId, 1)       // 匹配条件
                   .allowUpdateKey()         // 允许更新主键
                   .updateTo(User::getId, 2) // 更新主键
                   .doUpdate();
// 返回 result 为受影响的行数
```

## 空条件

不指定任何条件的更新是一项十分危险的操作，默认情况下 dbVisitor 会禁止此类操作。

若想不指定条件更新整张表的数据需要调用 allowEmptyWhere 方法以打开此次查询的空条件。

```java
LambdaTemplate lambda = ...
int result = lambda.update(User.class)
                   .allowEmptyWhere()            // 允许本次更新不指定条件
                   .updateTo(User::getStatus, 2) // 整张表的 status 字段都更新成 2
                   .doUpdate();
// 返回 result 为受影响的行数
```
