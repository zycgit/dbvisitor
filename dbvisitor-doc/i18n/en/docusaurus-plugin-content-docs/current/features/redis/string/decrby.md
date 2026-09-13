---
id: decrby
sidebar_position: 6
title: DECRBY
---

:::info[Note]
Official reference: [DECRBY](https://redis.io/docs/latest/commands/decrby/).
:::

Decrease a counter by an integer.

## Syntax

```text
DECRBY key decrement
```

Values and increments must be signed 64-bit integers. Non-integer content or overflow causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, LONG type |

## Example

```text
SET demo:counter 10
DECRBY demo:counter 3
```
