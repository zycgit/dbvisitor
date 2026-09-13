---
id: bzpopmin
sidebar_position: 6
title: BZPOPMIN
---

:::info[Note]
Official reference: [BZPOPMIN](https://redis.io/docs/latest/commands/bzpopmin/).
:::

Wait and pop the lowest-scoring member from the first nonempty sorted set.

## Syntax

```text
BZPOPMIN key [key ...] timeout
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
BZPOPMIN demo:ranking 1
```
