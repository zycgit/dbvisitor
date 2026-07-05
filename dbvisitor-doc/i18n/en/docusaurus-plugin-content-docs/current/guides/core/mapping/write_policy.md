---
id: write_policy
sidebar_position: 5
title: Write Policy
description: Configure how fields participate in database writes with dbVisitor ORM.
---

# Write Policy

When using the [Fluent API](../../api/lambda), set column write policies to control INSERT/UPDATE behavior.

```java title='Disallow updates: excluded from UPDATE SET'
@Table
public class Users {
    ...
    @Column(name = "create_time", update = false) // Column excluded from updates
    private Date    createTime;
    ...
}
```

```java title='Disallow inserts: excluded from INSERT values'
@Table
public class Users {
    ...
    @Column(name = "create_time", insert = false) // Column excluded from inserts
    private Date    createTime;
    ...
}
```
