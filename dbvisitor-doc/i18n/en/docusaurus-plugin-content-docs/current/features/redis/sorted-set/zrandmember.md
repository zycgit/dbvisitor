---
id: zrandmember
sidebar_position: 19
title: ZRANDMEMBER
---

:::info[Note]
Official reference: [ZRANDMEMBER](https://redis.io/docs/latest/commands/zrandmember/).
:::

Read random members, optionally with scores.

## Syntax

```text
ZRANDMEMBER key [count [WITHSCORES]]
```

Omit `count` for one member; positive counts return distinct members and negative counts allow repeats. `WITHSCORES` must follow `count`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZRANDMEMBER demo:ranking 2 WITHSCORES
```
