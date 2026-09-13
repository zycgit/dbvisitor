---
id: zdiffstore
sidebar_position: 11
title: ZDIFFSTORE
---

:::info[Note]
Official reference: [ZDIFFSTORE](https://redis.io/docs/latest/commands/zdiffstore/).
:::

Store a sorted-set difference.

## Syntax

```text
ZDIFFSTORE destination numkeys key [key ...]
```

`numkeys` must match the number of following source keys.

The result overwrites the destination; an empty result removes it.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements in the resulting sorted set. |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZDIFFSTORE demo:diff 2 demo:ranking demo:other
```
