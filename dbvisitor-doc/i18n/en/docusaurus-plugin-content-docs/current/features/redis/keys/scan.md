---
id: scan
sidebar_position: 23
title: SCAN
---

:::info[Note]
Official reference: [SCAN](https://redis.io/docs/latest/commands/scan/).
:::

Read one batch of key names and its continuation cursor.

## Syntax

```text
SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]
```

Start with cursor `0`. `MATCH` filters names, `COUNT` hints at scan work, and `TYPE` accepts Redis type names such as `string`, `hash`, `list`, `set` and `zset`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | CURSOR field, STRING type<br/>KEY field, STRING type |

:::caution[Caution]
The cursor is attached to result rows, so empty batches lose the continuation cursor. COUNT also caps returned rows and may truncate a batch. Do not use this path for complete traversal. Use [KEYS](keys.md) for pattern-based key retrieval, or a Redis client to manage cursors yourself.
:::

## Example

```text
SET demo:message hello
SCAN 0 MATCH demo:* COUNT 10 TYPE string
```
