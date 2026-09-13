---
id: sscan
sidebar_position: 15
title: SSCAN
---

:::info[Note]
Official reference: [SSCAN](https://redis.io/docs/latest/commands/sscan/).
:::

Read one cursor batch of set members.

## Syntax

```text
SSCAN key cursor [MATCH pattern] [COUNT count]
```

Start with cursor `0`. `MATCH` filters members; `COUNT` hints at scan work, not a fixed page size.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | CURSOR field, STRING type<br/>ELEMENT field, STRING type |

:::caution[Caution]
The cursor is attached to each result row, so empty batches lose the continuation cursor. Use a Redis client for guaranteed complete traversal; an empty batch does not mean completion.
:::

## Example

```text
SADD demo:tags java jdbc
SSCAN demo:tags 0 MATCH j* COUNT 10
```
