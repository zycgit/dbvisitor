---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 类型处理器
description: 类型处理器用于在 Java 类型与数据库字段类型之间进行转换读写，例如使用 String 读写 VARCHAR 类型数据。
---

类型处理器（TypeHandler）用于在 Java 类型与数据库字段类型之间进行转换读写，例如：使用 `String` 读写数据库 `VARCHAR` 类型数据。

通过类型处理器可以有效解决数据库一些特殊类型的读写处理，如：[地理信息类型](./gis-handler)、货币类型、时区时间、[枚举](./enum-handler)、[序列化](./json-serialization)。

## 类型处理器选择优先级

在没有明确指定类型处理器时，dbVisitor 将按下列优先级自动选择：

1. `Java 类型` + `JDBC 类型`（[组合类型表](./handlers/about#mix_type)）
2. `Java 类型`（[单一类型表](./handlers/about#single_type)），**最常用**
3. `JDBC 类型`
4. 使用 `UnknownTypeHandler`（兜底）

```text title='示例：通过 javaType 指定'
select * from users where name = #{name, javaType=java.lang.String}
```

- Java 类型 `java.lang.String` 对应 `StringTypeHandler`（见 [单一类型表](./handlers/about#single_type)）。

```text title='示例：通过 javaType + jdbcType 指定'
select * from users where name = #{name, jdbcType=varchar, javaType=java.lang.String}
```

- JDBC 类型 `VARCHAR` + Java 类型 `java.lang.String` 对应 `StringTypeHandler`（见 [组合类型表](./handlers/about#mix_type)）。

:::info
- 大多数类型处理器在读写数据时无需指定 JDBC 类型。
- 通常情况下参数设置和结果集读取只需指定 Java 类型即可（存储过程 OUT 参数除外）。
:::

如果内置类型处理器无法满足需要，可以通过 [自定义类型处理器](./custom-handler) 来处理特殊情况。

## 使用指引

- [基础类型处理器](./handlers/about)，了解 dbVisitor 已内置的丰富的类型处理器。
- [枚举类型处理](./enum-handler)，枚举类型通常会自动选用 `EnumTypeHandler`，支持自定义映射。
- [序列化处理器](./json-serialization)，将 Java 对象以 JSON 格式序列化到数据库。
- [地理信息类型](./gis-handler)，基于 JTS 读写 OpenGIS 的 WKT/WKB 地理信息数据。
- [流类型处理器](./stream-handler)，通过 InputStream/Reader 读写流数据。
- [数组类型](./array-handler)，处理数组类型及 PostgreSQL 特殊类型（pgvector 等）的读写。
