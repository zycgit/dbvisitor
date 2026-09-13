---
id: zpopmin
sidebar_position: 5
title: ZPOPMIN
---

:::info[Note]
Official reference: [ZPOPMIN](https://redis.io/docs/latest/commands/zpopmin/).
:::

Pop the lowest-scoring members.

## Syntax

```text
ZPOPMIN key [count]
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
ZPOPMIN demo:ranking 1
```
