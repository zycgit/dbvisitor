---
id: custom-handler
sidebar_position: 2
title: 8.2 自定义类型处理器
description: 当 dbVisitor 所提供的类型处理器无法满足需要时，可以根据自身需要自定义类型处理器。
---

# 自定义类型处理器

当 dbVisitor 所提供的类型处理器无法满足需要时，可以根据自身需要自定义类型处理器。

继承 `AbstractTypeHandler<T>` 并实现 4 个抽象方法即可：

```java title='演示：将字符串以 Timestamp 类型方式写入数据库'
package net.demos.dto;

public class MyDateTypeHandler extends AbstractTypeHandler<String> {
    public void setNonNullParameter(PreparedStatement ps, int i,
            String parameter, Integer jdbcType) throws SQLException {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parameter);
            ps.setTimestamp(i, new Timestamp(date.getTime()));
        } catch (ParseException e) {
            throw new SQLException(e);
        }
    }

    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return fmtDate(rs.getTimestamp(columnName));
    }

    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return fmtDate(rs.getTimestamp(columnIndex));
    }

    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return fmtDate(cs.getTimestamp(columnIndex));
    }

    private String fmtDate(Timestamp sqlTimestamp) {
        if (sqlTimestamp != null) {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date(sqlTimestamp.getTime()));
        }
        return null;
    }
}
```

## 显示引用

显式引用是最常见的使用方式，即在 SQL 语句或代码中明确指定使用的类型处理器。

```java title='在参数传递中使用自定义类型处理器'
String time = "2019-10-11";
jdbc.queryForList("select * from users where create_time = #{arg0, typeHandler=net.demos.dto.MyDateTypeHandler}", time);
```

- 查询中使用了 `#{...}` 写法，利用 [位置参数名称化](../args/position#pos_named) 方式传递参数。
- 借助 [typeHandler](../args/options#normal) 参数选项，设置 MyDateTypeHandler 类处理参数的读写请求。

```java title='在对象映射中使用自定义类型处理器'
public class User {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;

    // getters and setters omitted
}

// 查询参数
jdbc.queryForList("select * from users where id > ?", 2, User.class);
```

```xml title='在 XML 文件中定义实体时使用自定义类型处理器'
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper>
    <entity table="users" type="net.demos.dto.User">
        ...
        <mapping column="my_time" property="myTime" typeHandler="net.demos.dto.MyDateTypeHandler"/>
        ...
    </entity>
</mapper>
```

```xml title='在 XML 文件中定义结果集映射时使用自定义类型处理器'
<!DOCTYPE mapper PUBLIC "-//dbvisitor.net//DTD Mapper 1.0//EN"
                        "https://www.dbvisitor.net/schema/dbvisitor-mapper.dtd">
<mapper namespace="net.demos.dto">
    <resultMap id="user_resultMap" type="net.demos.dto.User">
        ...
        <result column="my_time" property="myTime" typeHandler="net.demos.dto.MyDateTypeHandler"/>
        ...
    </resultMap>
</mapper>
```

## 隐式引用

隐式方式用于替换 dbVisitor 提供的默认类型处理器，或为某个全新类型添加默认支持。通过注册方式实现，无需在每个使用处显式指定。

```java title='使用自定义类型处理器替代默认 StringTypeHandler'
// 自定义处理器
@MappedJavaTypes(String.class)
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

// 通过 registerHandler 注册，会自动读取注解并绑定到 String.class
TypeHandlerRegistry.DEFAULT.registerHandler(MyStringTypeHandler.class, new MyStringTypeHandler());

// 此时 dbVisitor 所有涉及 String 类型的读写都会使用 MyStringTypeHandler
// User 类无需通过 typeHandler 属性明确指定
jdbc.queryForList("select * from user_table where name = ?", arg, User.class);
```

:::info
`TypeHandlerRegistry.DEFAULT` 是全局类型处理器注册中心，如需要特殊定制可以 new 一个。
:::

## 类型绑定

### 绑定到 Java 类型

```text title='查询示例'
select * from users where name = #{name, javaType=java.lang.String}
```

```java title='编程方式注册'
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.register(String.class, new MyStringTypeHandler());
```

```java title='注解方式注册'
@MappedJavaTypes(String.class)
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.registerHandler(MyStringTypeHandler.class, new MyStringTypeHandler());
```

### 绑定到 JDBC 类型

```text title='查询示例'
select * from users where name = #{name, jdbcType=varchar}
```

```java title='编程方式注册'
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.register(Types.VARCHAR, new MyStringTypeHandler());
```

```java title='注解方式注册'
@MappedJdbcTypes(Types.VARCHAR)
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.registerHandler(MyStringTypeHandler.class, new MyStringTypeHandler());
```

### 类型交叉绑定

```text title='查询示例'
select * from users where name = #{name, jdbcType=nvarchar, javaType=java.lang.String}
```

```java title='编程方式注册'
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.register(Types.NVARCHAR, String.class, new MyStringTypeHandler());
```

```java title='注解方式注册'
@MappedCrossTypes(javaType = String.class, jdbcType = Types.NVARCHAR)
public class MyStringTypeHandler extends AbstractTypeHandler<String> {
    ...
}

TypeHandlerRegistry typeRegistry = ...;
typeRegistry.registerHandler(MyStringTypeHandler.class, new MyStringTypeHandler());
```

## 处理器参数

dbVisitor 允许自定义类型处理器具有一个类型为 `Class` 的构造方法作为参数。

比如：当查询参数为枚举时或者结果集映射到对象中的枚举字段时，类型处理器需要知道将字段值如何翻译成枚举对象。
- **处理器参数** 便可让类型处理器感知到具体正在操作的 Java 类型（dbVisitor 内置的 `EnumTypeHandler` 便是利用此机制）

```java title='用法'
public class MyTypeHandler extends AbstractTypeHandler<Object> {
    public MyTypeHandler(Class<?> argType) {
        ...
    }
}
```

### @NoCache 注释

注册器 TypeHandlerRegistry 类具备缓存机制，目的是为了加速 TypeHandler 使用过程中的创建和获取步骤。

当类型处理器使用了处理器参数后，缓存机制可能会命中到已创建的 typeHandler 而忽略相同 typeHandler 但参数不同的情况。例如：

```text title='示例：两个查询使用相同 TypeHandler 但不同参数类型'
select * from users 
where user_type = #{arg0, javaType= net.demos.dto.UserTypeEnum, ➊
                          typeHandler=net.demos.dto.MyTypeHandler}


select * from users 
where auth_type = #{arg0, javaType= net.demos.dto.AuthTypeEnum, ➋
                          typeHandler=net.demos.dto.MyTypeHandler}
```

- ➊,➋ 两次查询使用了相同的类型处理器 MyTypeHandler 来处理不同的 Java 枚举类型。
- 在不使用 `@NoCache` 注解的情况下第二次查询 dbVisitor 会把参数当成 UserTypeEnum 进行处理，这就造成了混乱。


通过 `@NoCache` 注解可以避免缓存机制的干扰，具体用法如下：

```java
@NoCache
public class MyTypeHandler extends AbstractTypeHandler<Object> {
    public MyTypeHandler(Class<?> argType) {
        ...
    }
}
```

:::info
任何主动将 TypeHandler 注册到 TypeHandlerRegistry 的操作 @NoCache 都不会对其产生影响。

比如：通过如下三种方式，无论是否具有 @NoCache 标志依然能够成功将 MyTypeHandler 实例对象永久绑定到对应类型组合中。

```java
typeRegistry.registerHandler(MyTypeHandler.class, new MyTypeHandler());
typeRegistry.register(String.class, new MyTypeHandler());
typeRegistry.register(Types.NVARCHAR, new MyTypeHandler());
typeRegistry.register(Types.NVARCHAR, String.class, new MyTypeHandler());
```
:::

只有当使用 TypeHandlerRegistry 的 createTypeHandler 方法被动创建 TypeHandler 时 @NoCache 才会生效。
