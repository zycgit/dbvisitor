---
id: blpop
sidebar_position: 7
title: BLPOP
---

:::info[Note]
Official reference: [BLPOP](https://redis.io/docs/latest/commands/blpop/).
:::

Wait for an element and pop from the left end.

## Syntax

```text
BLPOP key [key ...] timeout
```

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely. The example inserts elements before waiting.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

:::caution[Caution]
With data, returns two `ELEMENT` rows: the key name first, then the element, not two columns in one row. Empty-queue timeouts can currently fail; use a Redis client for consumers requiring reliable timeout handling.
:::

## Example

```text
RPUSH demo:queue first second
BLPOP demo:queue 1
```
