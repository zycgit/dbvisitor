---
id: fields
slug: /features/milvus/sql/types
sidebar_position: 1
title: Scalar Types
---

| Field type | Description |
| --- | --- |
| `BOOL` | Boolean; use TRUE or FALSE. |
| `INT8` | -128 ~ 127 |
| `INT16` | -32768 ~ 32767 |
| `INT32` | -2147483648 ~ 2147483647 |
| `INT64` | -9223372036854775808 ~ 9223372036854775807 |
| `FLOAT` | 32-bit floating point. |
| `DOUBLE` | 64-bit floating point. |
| `VARCHAR(n)` | String; n is the maximum UTF-8 byte length. |

Primary keys use INT64 or VARCHAR(n). See [field constraints](defaults.md) for NULL and DEFAULT.
