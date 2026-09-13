---
id: incr
sidebar_position: 3
title: INCR
---

:::info[Note]
Official reference: [INCR](https://redis.io/docs/latest/commands/incr/).
:::

Increment an integer by one, starting from zero for a missing key.

## Syntax

```text
INCR key
```

Values and increments must be signed 64-bit integers. Non-integer content or overflow causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, LONG type |

## Example

```text
SET demo:counter 10
INCR demo:counter
```
