---
id: lmove
sidebar_position: 1
title: LMOVE
---

:::info[Note]
Official reference: [LMOVE](https://redis.io/docs/latest/commands/lmove/).
:::

Move one element between specified ends of two lists.

## Syntax

```text
LMOVE source destination LEFT|RIGHT LEFT|RIGHT
```

The first `LEFT/RIGHT` selects the source end; the second selects the destination end.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
LMOVE demo:queue demo:processing LEFT RIGHT
```
