---
id: lambda
sidebar_position: 3
hide_table_of_contents: true
title: Call Fluent API
description: Through the parameterless insert, update, delete, query of the BaseMapper interface, you can directly use the Fluent API for database operations without data preparation.
---

# Call Fluent API

Through the parameterless `insert`, `update`, `delete`, `query` of the `BaseMapper` interface, you can directly use the [Fluent API](../lambda/about#principle) for database operations without data preparation.

You can choose the appropriate way to obtain a Session according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

```java title='Example: Insert'
User user = ...

BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.insert().applyEntity(user);
               .executeSumResult();
```

```java title='Example: Update'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.update().eq(User::getId, 1)              // Matching condition
               .updateTo(User::getName, "Mary") // Update field, using Lambda
               .updateTo(User::getStatus, 2)    // Multiple fields can be updated via method chaining
               .doUpdate();
```

```java title='Example: Delete'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
mapper.delete().eq(User::getId, 1) // Matching condition
               .doDelete();
```

```java title='Example: Query'
BaseMapper<User> mapper = session.createBaseMapper(User.class);
List<User> result = null;
result = mapper.query().le(User::getId, 100)   // Match ID less than or equal to 100
                       .queryForList();        // Map result set to entity type
```

:::info[For detailed usage of Fluent API, please refer to:]
- Please refer to [LambdaTemplate Class User Guide](../lambda/about#guide)
:::