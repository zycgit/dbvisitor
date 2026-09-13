---
id: brpop
sidebar_position: 8
title: BRPOP
---

:::info[Note]
Official reference: [BRPOP](https://redis.io/docs/latest/commands/brpop/).
:::

Wait for an element and pop from the right end.

## Syntax

```text
BRPOP key [key ...] timeout
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
BRPOP demo:queue 1
```
