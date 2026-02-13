---
id: type
sidebar_position: 6
hide_table_of_contents: true
title: 类型映射和处理
description: 使用 dbVisitor ORM 映射字段类型和 jdbcType。
---

# 类型映射和处理

如果一个 int 类型的 Java 属性映射到数据库的 int 类型上，对于这个属性类型映射而言将会有三个重要的参数：
- ➊ 属性所使用的 Java 类型。
- ➋ 属性映射列的 JDBC 类型。
- ➌ 用于数据读写的类型处理器。

```java
@Table
public class Users {
    @Column(➋ jdbcType = java.sql.Types.TINYINT, ➌ typeHandler = IntegerTypeHandler.class)
    private ➊ Integer id;
}
```

:::info
在大部分情况下都无需操心 jdbcType 和 typeHandler 参数框架会根据其 Java 类型来自动选择。
:::

### 处理抽象类型

```java
@Table
public class Users {
    @Column(specialJavaType = Integer.class)
    private Number counter;
}
```

### 处理枚举类型

```java
@Table
public class Users {
    @Column
    private UserTypeEnum type; // 框架自动兼容，无需特殊处理
}
```

- 更多有关枚举类型映射参考 [枚举类型处理器](../../types/enum-handler) 的内容。

### 处理 JSON 序列化

```java title="使用 @Column 注解绑定 Json 序列化器"
@Table
public class Users {
    @Column(typeHandler = net.hasor.dbvisitor.types.handler.json.JsonTypeHandler.class)
    private UserExtInfo moreInfo; // 属性会使用 JSON 结构进行序列化/反序列化
}
```

```java title="使用 @BindTypeHandler 为类型设置序列化器"
@BindTypeHandler(net.hasor.dbvisitor.types.handler.json.JsonTypeHandler.class)
public class UserExtInfo {
    ...
}

@Table
public class Users {
    private UserExtInfo moreInfo;
}
```

### 使用自定义类型处理器

```java
public class User {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;
}
```

- 更多有关内容参考 [自定义类型处理器](../../types/custom-handler)。
