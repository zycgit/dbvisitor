---
id: incrby
sidebar_position: 4
title: INCRBY
---

:::info[Note]
Official reference: [INCRBY](https://redis.io/docs/latest/commands/incrby/).
:::

Increase a counter by an integer.

## Syntax

```text
INCRBY key increment
```

Values and increments must be signed 64-bit integers. Non-integer content or overflow causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, LONG type |

## Example

```text
SET demo:counter 10
INCRBY demo:counter 5
```
