---
sidebar_position: 3
title: 冲突策略
description: 使用 dbVisitor ORM 工具，处理数据写入冲突的情况。
---
# 冲突策略

向数据库插入重复数据通常并非有意而为之，而一旦出现主键冲突就会比较麻烦。一般的解决办法是再次尝试按照主键更新或者放弃这条数据。

dbVisitor 中对于这种情况可以配置冲突策略，这样就可以避免在写入数据时多余的代码逻辑。可选的冲突策略有三个（报错、替换、忽略）

## 报错

报错是默认策略无需特别指定，这个策略下将会使用普通的 `insert into` 语句进行数据插入。

```java
TestUser testUser = ...
InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyEntity(testUser).executeSumResult();
```

由于默认就是 `into` 因此下面这段代码和上面的是等价的

```java {4}
TestUser testUser = ...
InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyEntity(testUser)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Into)
                   .executeSumResult();
```

## 替换

替换策略的实现是根据具体数据库 `方言` 实现决定

- 对于 MySQL 将会使用 ``ON DUPLICATE KEY UPDATE`` 字句修饰 insert。
- 对于 Oracle 将会使用 ``MERGE INTO ... WHEN MATCHED THEN ... WHEN NOT MATCHED THEN ...`` 语句。

:::tip
是否支持以及实现形式，以数据库方言为准
:::

使用 `替换` 策略只需要简单的设置一下策略属性

```java {4}
TestUser testUser = ...
InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyEntity(testUser)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Update)
                   .executeSumResult();
```

## 忽略

忽略策略的实现是根据具体数据库 `方言` 实现决定

- 对于 MySQL 将会使用 `INSERT IGNORE` 语句。
- 对于 Oracle 将会使用 `MERGE INTO ... WHEN NOT MATCHED THEN ...` 语句。

:::tip
是否支持以及实现形式，以数据库方言为准。详情查看 **[分页与方言](../page.mdx)**
:::

使用 `忽略` 策略只需要简单的设置一下策略属性

```java {4}
TestUser testUser = ...
InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
int result = insert.applyEntity(testUser)
                   .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)
                   .executeSumResult();
```
