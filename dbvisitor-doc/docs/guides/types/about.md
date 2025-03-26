---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 类型处理器
description: 类型处理器是用于将一个 Java 类型读写某个数据库表的字段类型，例如：使用 String 读写数据库 VARCHAR 类型数据。
---

类型处理器是用于将一个 Java 类型读写某个数据库表的字段类型，例如：使用 String 读写数据库 VARCHAR 类型数据。

通过类型处理器可以有效解决数据库一些特殊类型的读写处理如：[地理信息类型](./gis-handler)、货币类型、时区时间、[枚举](./enum-handler)、[序列化](./json-serialization)。

在没有明确指定类型处理器时。dbVisitor 将会遵循下列优先级来确定参数和返回值类型的读写使用的具体类型处理器：
- 1st `Java 类型` + `JDBC 类型`
- 2st `Java 类型`，**最为常用**
- 3st `JDBC 类型`
- 4st 使用 `UnknownTypeHandler`

```text title='以参数传递为例：使用 Java 类型指定类型处理器'
select * from users where name = #{name, javaType=java.lang.String}
```

- JAVA 类型为 `java.lang.String`，根据 [常见类型处理器-单一类型](./handlers/about#single_type) 表中可知。使用的类型处理器为 `StringTypeHandler`。

```text title='以参数传递为例：使用 Java + Jdbc'
select * from users where name = #{name, jdbcType=varchar, javaType=java.lang.String}
```

- JDBC 类型使用 `VARCHAR`，JAVA 类型为 `java.lang.String`， 根据 [常见类型处理器-组合类型](./handlers/about#mix_type) 表中可知。使用的类型处理器为 `StringTypeHandler`。

:::info
- 在 dbVisitor 中大多数类型处理器在读写数据时是无需 JDBC 类型参数
- 因此通常情况下参数设置结果集读取只需要指定 Java 类型即可（存储过程 OUT 参数除外）。
:::

如果内置类型处理器无法满足程序需要，可以通过 [自定义类型处理器](./custom-handler) 来处理特殊情况。

## 使用指引

- [基础类型处理器](./handlers/about)，dbVisitor 提供了大量实用的类型处理器，当遇到类型问题可以先看下已有类型处理器是否已经支持。
- [枚举类型处理](./enum-handler)，对于枚举类型通常会自动选择 `EnumTypeHandler` 进行处理，一般情况下无需干预。
- [序列化处理器](./json-serialization)，通过 JSON 序列化处理器，可以将 Java 对象保存到数据库中。
- [地理信息类型](./gis-handler)，通过使用 JTS 可以非常方便的读写 OpenGIS 的地理信息类型数据如 WKB、WKB。
- [流类型处理器](./stream-handler)，通过使用 InputStream/Reader 类型处理器读写流数据。
- [数组类型](./array-handler)，用于处理数组类型数据的读写。
