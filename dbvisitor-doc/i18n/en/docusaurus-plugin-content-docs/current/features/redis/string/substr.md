---
id: substr
sidebar_position: 20
title: SUBSTR
---

:::info[Note]
Official reference: [SUBSTR](https://redis.io/docs/latest/commands/substr/).
:::

Read a substring by byte offsets; this is the old name of GETRANGE.

## Syntax

```text
SUBSTR key start end
```

Offsets start at zero and include the end position. Negative offsets count from the end; `-1` is the last byte.

Use [GETRANGE](getrange.md) in new code.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
SET demo:message hello
SUBSTR demo:message 0 3
```
