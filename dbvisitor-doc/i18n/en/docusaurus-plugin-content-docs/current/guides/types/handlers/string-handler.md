---
id: string-handler
sidebar_position: 4
title: String/Char Types
description: Type handlers for string and character types in dbVisitor.
---

<span id="stringchar-type-handlers" />

# String/Char Types

String/char type handlers are located in the `net.hasor.dbvisitor.types.handler.string` package.

## Common Handlers

| Handler | Java Type | Purpose |
|---|---|---|
| `StringTypeHandler` | `java.lang.String` | Reads/writes string data via getString/setString |
| `ClobAsStringTypeHandler` | `java.lang.String` | Reads/writes string data via getClob/setClob |
| `NStringTypeHandler` | `java.lang.String` | Reads/writes string data via getNString/setNString |
| `NClobAsStringTypeHandler` | `java.lang.String` | Reads/writes string data via getNClob/setNClob |

## Character Types

| Handler | Java Type | Purpose |
|---|---|---|
| `StringAsCharTypeHandler` | `java.lang.Character` | Reads/writes via getString/setString, only recognizes the first character |
| `NStringAsCharTypeHandler` | `java.lang.Character` | Reads/writes via getNString/setNString, only recognizes the first character |

## Type Conversions

| Handler | Java Type | Purpose |
|---|---|---|
| `StringAsUrlTypeHandler` | `java.net.URL` | Reads/writes URL type data |
| `StringAsUriTypeHandler` | `java.net.URI` | Reads/writes URI type data |

## Special Support

| Handler | Java Type | Purpose |
|---|---|---|
| `EnumTypeHandler` | `java.lang.Enum` | Enum type support, see [Enum Type Handling](../enum-handler) for details |
| `SqlXmlTypeHandler` | `java.lang.String` | Reads/writes XML strings via getSQLXML/setSQLXML |
