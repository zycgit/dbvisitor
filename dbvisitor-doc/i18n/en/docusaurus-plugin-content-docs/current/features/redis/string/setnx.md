---
id: setnx
sidebar_position: 17
title: SETNX
---

:::info[Note]
Official reference: [SETNX](https://redis.io/docs/latest/commands/setnx/).
:::

Write a string only if the key does not exist.

## Syntax

```text
SETNX key value
```

The corresponding `EX`, `PX` or `NX` option of [SET](set.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if key was set; 0 otherwise |

## Example

```text
SETNX demo:new-message hello
```
