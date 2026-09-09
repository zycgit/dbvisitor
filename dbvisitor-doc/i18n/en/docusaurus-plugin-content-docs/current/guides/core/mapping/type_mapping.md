---
id: type_mapping
sidebar_position: 6
title: Type Mapping and Handlers
description: Map field types and jdbcType with dbVisitor ORM.
---

# Type Mapping and Handlers

When a Java `int` maps to a database `int`, three aspects matter:
- Java type used by the property
- JDBC type of the column
- TypeHandler for reading/writing

```java
@Table
public class Users {
    @Column(jdbcType = java.sql.Types.TINYINT, typeHandler = IntegerTypeHandler.class)
    private Integer id;
}
```

:::info
In most cases you can ignore `jdbcType` and `typeHandler`; the framework selects them from the Java type.
:::

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

### Handle JSON serialization

```java title="Bind JSON serializer via @Column"
@Table
public class Users {
    @Column(typeHandler = net.hasor.dbvisitor.types.handler.json.JsonTypeHandler.class)
    private UserExtInfo moreInfo; // Field is serialized/deserialized as JSON
}
```

```java title="Set serializer for a type via @BindTypeHandler"
@BindTypeHandler(net.hasor.dbvisitor.types.handler.json.JsonTypeHandler.class)
public class UserExtInfo {
    ...
}

@Table
public class Users {
    private UserExtInfo moreInfo;
}
```

### Use a custom TypeHandler

```java
public class User {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;
}
```

- See [Custom TypeHandler](../../types/custom-handler) for more details.
