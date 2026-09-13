---
id: hincrby
sidebar_position: 11
title: HINCRBY
---

:::info[Note]
Official reference: [HINCRBY](https://redis.io/docs/latest/commands/hincrby/).
:::

Increase a hash field by an integer.

## Syntax

```text
HINCRBY key field increment
```

`increment` is a signed 64-bit integer; a missing field starts at zero.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, LONG type |

## Example

```text
HSET demo:user age 18
HINCRBY demo:user age 1
```
