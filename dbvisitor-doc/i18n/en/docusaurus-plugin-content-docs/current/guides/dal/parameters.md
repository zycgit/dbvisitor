---
sidebar_position: 3
title: 参数化
description: 通过 dbVisitor ORM 工具的 DalSession 类执行 Mapper 文件中定义的 SQL。
---

# 参数化

在 **[映射文件](./dal-mapper.md)** 和 **[动态 SQL](./dynamic.md)** 两部分内容中，看到了简单的参数传递，例 `#{age}`：

```xml
<select id="queryListByAge">
    select * from `test_user` where age = #{age}
</select>
```

参数名是根据 API 的调用时候穿入的，具体可以查阅 **[Mapper 接口](./mapper-api.md)**，本节将会介绍 dbVisitor 提供的三种不同参数传递方式。

- `#{...}` 动态参数。
- `${}` 字符串替换，使用时要小心 SQL 注入
- `@{}` 规则表达式，灵活使用规则可以减少大量 XML 配置。

## 动态参数

在前面例子中 `#{age}` 会获取，当前参数对象 的 `age` 属性，并将它的值传递给 `PreparedStatement`。 这种传参方式是标准做法，通常我们所指的 SQL 防注入也是通过动态参数来解决。

偶尔我们会希望参数类型以某个特定的 JDBC Type 来传递，那么就可以通过下列方式穿入。

```xml
#{property, jdbcType=NUMERIC}
```

也可以同时指定 `jdbcType` 和 `javaType`，设置它们最大的作用是可以帮助我们选择适合的 **[类型处理器](../types/type-handlers.md)**。

```xml
#{property, javaType=int, jdbcType=NUMERIC}
```

有些时候需要指定 `TypeHandler` 比如我们自定义的 TypeHandler，可以选择下面这种方式。

```xml
#{property, typeHandler=net.hasor.dbvisitor.example.mapper.MyTypeHandler}
```

在执行存储过程通常需要指定输入输出参数，可以通过 `mode` 属性设置为 out 来确定输出参数，比如：

```xml
{call proc_select_user(#{abc, mode=out})}
```

在调用存储过程时 in 参数 和 out 参数都会通过在调用 API 时的参数来承载。mode 有三个取值 `in`、`out`、`inout` 其中默认是 `in`

## 使用注入

默认情况下，使用 `#{}` 语法将会使用 `PreparedStatement` 设置动态属性。 虽然这样更安全、更快，而且几乎总是首选，但有时只想直接将未修改的字符串注入 SQL 语句。 例如，对于 `order by`，你可以这样使用：

```xml
order by ${columnName}
```
