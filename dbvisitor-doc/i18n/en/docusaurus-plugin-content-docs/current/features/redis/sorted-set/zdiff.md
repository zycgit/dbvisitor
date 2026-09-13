---
id: zdiff
sidebar_position: 10
title: ZDIFF
---

:::info[Note]
Official reference: [ZDIFF](https://redis.io/docs/latest/commands/zdiff/).
:::

Read the difference between the first sorted set and the others.

## Syntax

```text
ZDIFF numkeys key [key ...] [WITHSCORES]
```

`numkeys` must match the number of following source keys.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type (when using WITHSCORES) |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZDIFF 2 demo:ranking demo:other WITHSCORES
```
