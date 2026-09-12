---
id: type_mapping
sidebar_position: 6
title: Type Mapping and Handlers
description: Map field types and jdbcType with dbVisitor ORM.
---

# Type Mapping and Handlers

Ordinary properties usually only need a Java type and column name; dbVisitor selects the corresponding TypeHandler:

```java
import net.hasor.dbvisitor.mapping.Column;

@Column("age")
private Integer age;
```

Configure typeHandler explicitly when using a special storage format. Use specialJavaType for a concrete Java type and jdbcType when controlling the underlying binding type; ordinary properties generally do not need these settings.

### Handle abstract types

```java
@Table
public class Users {
    @Column(specialJavaType = Integer.class)
    private Number counter;
}
```

### Handle enums

```java
@Table
public class Users {
    @Column
    private UserTypeEnum type; // Automatically supported; no extra work
}
```

- For more on enum mapping, see [Enum type handler](../../types/enum-handler).

### Use a custom TypeHandler

```java
public class User {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;
}
```

- See [Custom TypeHandler](../../types/custom-handler) for more details.
