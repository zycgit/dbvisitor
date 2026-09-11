---
id: bool-handler
sidebar_position: 2
title: Boolean Types
description: Type handler for boolean types in dbVisitor.
---

<span id="boolean-type-handler" />

# Boolean Types

Boolean type handlers are located in the `net.hasor.dbvisitor.types.handler.bool` package.

## Handler List

| Handler | Java Type | Purpose |
|---|---|---|
| `BooleanTypeHandler` | `java.lang.Boolean`, `boolean` | Reads and writes boolean type data |

## Integer Boolean Values

:::tip
If the database stores boolean values as integers (0/1), you can use `IntegerAsBooleanTypeHandler` (in the number package). Any non-zero integer will be parsed as `true`.
:::
