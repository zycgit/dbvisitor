---
id: bzpopmax
sidebar_position: 4
title: BZPOPMAX
---

:::info[Note]
Official reference: [BZPOPMAX](https://redis.io/docs/latest/commands/bzpopmax/).
:::

Wait and pop the highest-scoring member from the first nonempty sorted set.

## Syntax

```text
BZPOPMAX key [key ...] timeout
```

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |

:::caution[Caution]
When all source sets are empty (or a blocking call times out), the current driver can fail rather than reliably return an empty ResultSet. Use a Redis client when this case must be handled.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
BZPOPMAX demo:ranking 1
```
