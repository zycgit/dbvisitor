---
id: zrem
sidebar_position: 26
title: ZREM
---

:::info[Note]
Official reference: [ZREM](https://redis.io/docs/latest/commands/zrem/).
:::

Remove specified members.

## Syntax

```text
ZREM key member [member ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of members removed from the sorted set, not including non-existing members. |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZREM demo:ranking alice
```
