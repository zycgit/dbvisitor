---
id: getrange
sidebar_position: 10
title: GETRANGE
---

:::info[Note]
Official reference: [GETRANGE](https://redis.io/docs/latest/commands/getrange/).
:::

Read a substring by byte offsets.

## Syntax

```text
GETRANGE key start end
```

Offsets start at zero and include the end position. Negative offsets count from the end; `-1` is the last byte.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
SET demo:message hello
GETRANGE demo:message 0 3
```
