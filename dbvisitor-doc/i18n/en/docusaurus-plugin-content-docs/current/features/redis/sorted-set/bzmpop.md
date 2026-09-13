---
id: bzmpop
sidebar_position: 2
title: BZMPOP
---

:::info[Note]
Official reference: [BZMPOP](https://redis.io/docs/latest/commands/bzmpop/).
:::

Wait for members and pop from a sorted set.

## Syntax

```text
BZMPOP timeout numkeys key [key ...] MIN|MAX [COUNT count]
```

`numkeys` must match the key count. Pop from the first nonempty key. `MIN/MAX` choose lowest/highest scores; `COUNT` is the maximum to pop, default 1.

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |

:::caution[Caution]
When all source sets are empty (or a blocking call times out), the current driver can fail rather than reliably return an empty ResultSet. Use a Redis client when this case must be handled.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
BZMPOP 1 1 demo:ranking MIN COUNT 2
```
