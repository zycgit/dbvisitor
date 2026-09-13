---
id: decr
sidebar_position: 5
title: DECR
---

:::info[Note]
Official reference: [DECR](https://redis.io/docs/latest/commands/decr/).
:::

Decrement an integer by one, starting from zero for a missing key.

## Syntax

```text
DECR key
```

Values and increments must be signed 64-bit integers. Non-integer content or overflow causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, LONG type |

## Example

```text
SET demo:counter 10
DECR demo:counter
```
