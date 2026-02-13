---
id: string-handler
sidebar_position: 4
hide_table_of_contents: true
title: 字符类型
description: dbVisitor 处理字符类型的类型处理器。
---

# 字符类型处理器

字符类型处理器位于 `net.hasor.dbvisitor.types.handler.string` 包中。

## 常见处理器

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `StringTypeHandler` | `java.lang.String` | 以 getString/setString 方式处理字符串数据 |
| `ClobAsStringTypeHandler` | `java.lang.String` | 以 getClob/setClob 方式处理字符串数据 |
| `NStringTypeHandler` | `java.lang.String` | 以 getNString/setNString 方式处理字符串数据 |
| `NClobAsStringTypeHandler` | `java.lang.String` | 以 getNClob/setNClob 方式处理字符串数据 |

## 字符类型

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `StringAsCharTypeHandler` | `java.lang.Character` | 以 getString/setString 方式处理，对于字符串只识别第一个字符 |
| `NStringAsCharTypeHandler` | `java.lang.Character` | 以 getNString/setNString 方式处理，对于字符串只识别第一个字符 |

## 类型转换

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `StringAsUrlTypeHandler` | `java.net.URL` | 处理 URL 类型数据的读写 |
| `StringAsUriTypeHandler` | `java.net.URI` | 处理 URI 类型数据的读写 |

## 特殊支持

| 类型处理器 | Java 类型 | 作用 |
|---|---|---|
| `EnumTypeHandler` | `java.lang.Enum` | 枚举类型支持，详情见 [枚举类型处理](../enum-handler) |
| `SqlXmlTypeHandler` | `java.lang.String` | 以 getSQLXML/setSQLXML 方式处理 XML 字符串 |
