---
sidebar_position: 4
title: 自定义类型处理器
description: 通过 DalSession 执行 Mapper 文件中定义的 SQL。
---

# 自定义类型处理器

## 开发处理器

某些数据类型的写入需要特殊处理，例如将字符串数据按照某种格式转换成为时间日期类型。这就需要用到 `TypeHandler`。

例如，数据库中保存的是时间类型，需要将其读取成格式为 `yyyy-MM-dd` 的字符串。

```java {3}
@Table(mapUnderscoreToCamelCase = true)
public class TestUser {
    @Column(typeHandler = MyDateTypeHandler.class)
    private String myTime;

    // getters and setters omitted
}
```

类型读写器为：

```java
public class MyDateTypeHandler extends AbstractTypeHandler<String> {
    public void setNonNullParameter(PreparedStatement ps, int i,
                    String parameter, Integer jdbcType) {

        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parameter);
        ps.setTimestamp(i, new Timestamp(date.getTime()));
    }

    public String getNullableResult(ResultSet rs, String columnName) {
        return fmtDate(rs.getTimestamp(columnIndex));
    }

    public String getNullableResult(ResultSet rs, int columnIndex) {
        return fmtDate(rs.getTimestamp(columnIndex));
    }

    public String getNullableResult(CallableStatement cs, int columnIndex) {
        return fmtDate(cs.getTimestamp(columnIndex));
    }

    private String fmtDate(Timestamp sqlTimestamp) {
        if (sqlTimestamp != null) {
            Date date = new Date(sqlTimestamp.getTime());
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return null;
    }
}
```

:::tip
dbVisitor 内置了 70 多个 TypeHandler，基本涵盖了各种情况以及数据类型。详细的信息请看 **[类型处理器](./type-handlers.md)** 相关章节。
:::

## 绑定到 Jdbc Type

将类型处理器绑定到 `Types.VARCHAR` 上

```java
TypeHandlerRegistry.DEFAULT.register(Types.VARCHAR, new MyTypeHandler());
```

下面代码使用注解方式和上面是等效的

```java {1,7}
@MappedJdbcTypes(Types.VARCHAR)
public class MyTypeHandler extends AbstractTypeHandler<String> {
    ...
}

// 注册处理器
TypeHandlerRegistry.DEFAULT.register(MyTypeHandler.class, new MyTypeHandler());
```

:::tip
`TypeHandlerRegistry.DEFAULT` 是全局类型处理器注册中心，如需要特殊定制可以 new 一个。
:::

## 绑定到 Java Type

将类型处理器绑定到 `StringBuilder` 类型上

```java
TypeHandlerRegistry.DEFAULT.register(StringBuilder.class, new MyTypeHandler());
```

下面代码使用注解方式和上面是等效的

```java {1,7}
@MappedJavaTypes(StringBuilder.class)
public class MyTypeHandler extends AbstractTypeHandler<String> {
    ...
}

// 注册处理器
TypeHandlerRegistry.DEFAULT.register(MyTypeHandler.class, new MyTypeHandler());
```

## 交叉绑定

只有当 Java类型为 `InputStream` 并且 Jdbc Type 为 `Types.BIGINT` 时

```java
TypeHandlerRegistry.DEFAULT.registerCross(
            Types.BIGINT, InputStream.class, new MyTypeHandler());
```

下面代码使用注解方式和上面是等效的

```java {1-3,9}
@MappedCross(
        javaTypes = @MappedJavaTypes(InputStream.class), 
        jdbcType = @MappedJdbcTypes(Types.BIGINT))
public class MyTypeHandler extends AbstractTypeHandler<String> {
    ...
}

// 注册处理器
TypeHandlerRegistry.DEFAULT.register(MyTypeHandler.class, new MyTypeHandler());
```
