---
id: zscan
sidebar_position: 33
title: ZSCAN
---

:::info[Note]
Official reference: [ZSCAN](https://redis.io/docs/latest/commands/zscan/).
:::

Read one cursor batch of members and scores.

## Syntax

```text
ZSCAN key cursor [MATCH pattern] [COUNT count]
```

Start with cursor `0`. `MATCH` filters members; `COUNT` hints at scan work, not a fixed page size.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | CURSOR field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |

:::caution[Caution]
The cursor is attached to each result row, so empty batches lose the continuation cursor. Use a Redis client for guaranteed complete traversal; an empty batch does not mean completion.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZSCAN demo:ranking 0 MATCH a* COUNT 10
```
