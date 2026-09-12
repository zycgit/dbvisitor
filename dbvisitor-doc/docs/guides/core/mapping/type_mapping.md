---
id: type_mapping
sidebar_position: 6
title: 类型映射和处理
description: 使用 dbVisitor ORM 映射字段类型和 jdbcType。
---

# 类型映射和处理

普通属性通常只需声明 Java 类型和列名，dbVisitor 会选择对应的类型处理器：

```java
import net.hasor.dbvisitor.mapping.Column;

@Column("age")
private Integer age;
```

采用特殊存储格式时，再显式配置 `typeHandler`。`specialJavaType` 用于指定具体 Java 类型；`jdbcType` 用于需要控制底层绑定类型的情况，普通属性一般不必配置。

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

### 使用自定义类型处理器

```java
public class User {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;
}
```

- 更多有关内容参考 [自定义类型处理器](../../types/custom-handler)。
