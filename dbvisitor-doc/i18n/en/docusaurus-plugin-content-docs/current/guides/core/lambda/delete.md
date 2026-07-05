---
id: delete
sidebar_position: 4
title: Delete
description: Delete data with LambdaTemplate in dbVisitor.
---

# Delete

Delete data with `LambdaTemplate` as follows:

:::info[Tip]
For building conditions used in deletes, see **[where builder](./where_builder)**.
:::

```java
LambdaTemplate lambda = ...;
int result = lambda.delete(User.class)
                   .eq(User::getId, 1) // match condition
                   .doDelete();
// result is affected row count
```

## Empty conditions

Deleting without any condition is dangerous and is blocked by default. To delete an entire table intentionally, call `allowEmptyWhere` for that operation.

```java
LambdaTemplate lambda = ...;
int result = lambda.delete(User.class)
                   .allowEmptyWhere() // allow doDelete with no conditions
                   .doDelete();
// result is affected row count
```
