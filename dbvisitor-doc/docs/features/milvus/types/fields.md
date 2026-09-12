---
id: fields
slug: /features/milvus/sql/types
sidebar_position: 1
title: 标量类型
---

| 字段类型 | 说明 |
| --- | --- |
| `BOOL` | 布尔值，使用 TRUE 或 FALSE。 |
| `INT8` | -128 ~ 127 |
| `INT16` | -32768 ~ 32767 |
| `INT32` | -2147483648 ~ 2147483647 |
| `INT64` | -9223372036854775808 ~ 9223372036854775807 |
| `FLOAT` | 32 位浮点数。 |
| `DOUBLE` | 64 位浮点数。 |
| `VARCHAR(n)` | 字符串，n 为 UTF-8 字节数上限。 |

主键使用 INT64 或 VARCHAR(n)。NULL 和 DEFAULT 见[字段约束](defaults.md)。
