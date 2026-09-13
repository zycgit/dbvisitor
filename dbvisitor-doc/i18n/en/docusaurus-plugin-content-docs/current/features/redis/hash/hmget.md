---
id: hmget
sidebar_position: 14
title: HMGET
---

:::info[Note]
Official reference: [HMGET](https://redis.io/docs/latest/commands/hmget/).
:::

Read multiple fields in the requested order.

## Syntax

```text
HMGET key field [field ...]
```

One row per requested field, in argument order. Missing fields return null; there is no field-name column.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | VALUE field, STRING type |

## Example

```text
HSET demo:user name mali age 18
HMGET demo:user age name
```
