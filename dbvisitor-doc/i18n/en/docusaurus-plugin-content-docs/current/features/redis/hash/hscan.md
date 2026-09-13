---
id: hscan
sidebar_position: 22
title: HSCAN
---

:::info[Note]
Official reference: [HSCAN](https://redis.io/docs/latest/commands/hscan/).
:::

Read one cursor batch of hash fields and values.

## Syntax

```text
HSCAN key cursor [MATCH pattern] [COUNT count] [NOVALUES]
```

Start with cursor `0`. `MATCH` filters field names; `COUNT` hints at scan work; `NOVALUES` returns names only (requires Redis 7.4+).

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | CURSOR field, STRING type<br/>FIELD field, STRING type<br/>VALUE field, STRING type (when not using NOVALUES) |

:::caution[Caution]
The cursor is attached to each result row, so empty batches lose the continuation cursor. Use a Redis client for guaranteed complete traversal; an empty batch does not mean completion.
:::

## Example

```text
HSET demo:user name mali age 18
HSCAN demo:user 0 MATCH n* COUNT 10
```
