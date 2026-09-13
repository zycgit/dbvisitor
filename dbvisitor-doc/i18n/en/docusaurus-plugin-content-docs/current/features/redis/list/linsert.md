---
id: linsert
sidebar_position: 12
title: LINSERT
---

:::info[Note]
Official reference: [LINSERT](https://redis.io/docs/latest/commands/linsert/).
:::

Insert before or after the first matching pivot.

## Syntax

```text
LINSERT key BEFORE|AFTER pivot element
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the list after insert; returns 0 when key doesn't exist; returns -1 when pivot not found. |

## Example

```text
RPUSH demo:queue first second
LINSERT demo:queue AFTER first middle
```
