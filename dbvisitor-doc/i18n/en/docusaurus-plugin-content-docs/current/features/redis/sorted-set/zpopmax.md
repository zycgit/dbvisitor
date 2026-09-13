---
id: zpopmax
sidebar_position: 3
title: ZPOPMAX
---

:::info[Note]
Official reference: [ZPOPMAX](https://redis.io/docs/latest/commands/zpopmax/).
:::

Pop the highest-scoring members.

## Syntax

```text
ZPOPMAX key [count]
```

Omit `count` to pop one member; otherwise pop at most `count` members.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |

:::caution[Caution]
Omitting count on an empty set can currently fail. Supply an explicit count when the set may be empty, such as `1` in the example.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZPOPMAX demo:ranking 1
```
