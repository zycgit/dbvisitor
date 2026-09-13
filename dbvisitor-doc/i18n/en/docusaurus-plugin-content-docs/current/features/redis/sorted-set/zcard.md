---
id: zcard
sidebar_position: 8
title: ZCARD
---

:::info[Note]
Official reference: [ZCARD](https://redis.io/docs/latest/commands/zcard/).
:::

Read the sorted-set cardinality.

## Syntax

```text
ZCARD key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZCARD demo:ranking
```
