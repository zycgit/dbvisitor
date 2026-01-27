---
id: about
sidebar_position: 1
hide_table_of_contents: true
title: Mapper Interface
description: The generic BaseMapper interface provides a set of common database operation methods that utilize object mapping information to perform database operations.
---

The generic BaseMapper interface provides a set of common database operation methods that utilize object mapping information to perform database operations.

```java title='1. Declare Entity Class'
@Table("users")
public class User {
    @Column(name = "id", primary = true, keyType = KeyTypeEnum.Auto)
    private Long id;
    @Column("name")
    private String name;
    ...
}
```

```java title='2. Create Generic Mapper'
// 1. Create Configuration
Configuration config = new Configuration();

// 2. Create Session
Session session = config.newSession(dataSource);
// OR
Session session = config.newSession(connection);

// 3. Create BaseMapper
BaseMapper<User> mapper = session.createBaseMapper(User.class);
```

In actual usage, the way to obtain a Session may vary depending on your project architecture. The above code demonstrates a primitive way to create a Session.
You can choose the appropriate way to obtain a Session according to your project architecture. For details, please refer to: **[Framework Integration](../../yourproject/buildtools#integration)**

Relevant Classes
- net.hasor.dbvisitor.mapping.Table
- net.hasor.dbvisitor.mapping.Column
- net.hasor.dbvisitor.session.Configuration
- net.hasor.dbvisitor.session.Session
- net.hasor.dbvisitor.mapper.BaseMapper

## User Guide

The BaseMapper interface provides several methods. The main scenarios are introduced below:
- [Basic Operations](./common), common basic CRUD operations and pagination queries.
- [Invoke Fluent API](./lambda), use condition constructor on Mapper.
- [Execute commands in Mapper file](./file), used to define a command, which can be referenced on the Mapper interface where it resides.
