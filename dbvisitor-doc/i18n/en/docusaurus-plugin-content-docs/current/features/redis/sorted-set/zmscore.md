---
id: zmscore
sidebar_position: 18
title: ZMSCORE
---

:::info[Note]
Official reference: [ZMSCORE](https://redis.io/docs/latest/commands/zmscore/).
:::

Read several member scores in order.

## Syntax

```text
ZMSCORE key member [member ...]
```

One row per requested member in argument order; missing members have null scores.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | SCORE field, DOUBLE type |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZMSCORE demo:ranking alice bob
```
