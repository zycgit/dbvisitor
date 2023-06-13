---
sidebar_position: 4
title: 不安全的操作
description: dbVisitor ORM 默认不允许 无条件更新和无条件删除
---
# 不安全的操作

- 整表更新
- 更新主键列
- 更新整行
- 整表删除

## 整表更新

`整表更新` 会影响表中的所有数据，如果希望执行此类操作需要调用 `allowEmptyWhere` 方法允许空条件。

```java {2}
EntityUpdateOperation<TestUser> update = lambdaTemplate.lambdaUpdate(TestUser.class);
int result = update.allowEmptyWhere()
                   .updateBySample(sample)
                   .doUpdate();
```

## 更新主键列
`最低 v5.2.0`

`更新主键列` 主键一般是不具备业务属用于标识数据的 ID 标识。因此 ID 一直不提倡更新。

如果业务系统的确有需要将主键值进行变更那么需要调用 `allowUpdateKey` 方法允许更新主键列。

```java {2}
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
               .eq(TestUser::getId, 1)
               .allowUpdateKey()         // 允许更新主键
               .updateBySample(newData)
               .doUpdate();
```

## 更新整行

`最低 v5.2.0`

`更新整行` 整行更新是根据查询条件，将匹配的所有列都进行更新。这种操作容易引发意外的数据替换。

如果业务系统的确有需要将主键值进行变更那么需要调用 `allowReplaceRow` 方法允许更新整行。

```java {2}
int result = lambdaTemplate.lambdaUpdate(TestUser.class)
               .eq(TestUser::getId, 1)
               .allowReplaceRow()         // 允许更新整行
               .updateTo(newData)
               .doUpdate();
```

## 整表删除

`整表删除` 会删除所使用 `delete` 语句删除所有数据，如果希望执行此类操作需要调用 `allowEmptyWhere` 方法允许空条件。

```java {2}
EntityDeleteOperation<TestUser> delete = lambdaTemplate.lambdaDelete(TestUser.class);
int result = delete.allowEmptyWhere().doDelete()
```
