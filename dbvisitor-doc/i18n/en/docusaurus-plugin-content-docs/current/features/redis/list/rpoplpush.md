---
id: rpoplpush
sidebar_position: 9
title: RPOPLPUSH
---

:::info[Note]
Official reference: [RPOPLPUSH](https://redis.io/docs/latest/commands/rpoplpush/).
:::

Move the source tail element to the destination head.

## Syntax

```text
RPOPLPUSH source destination
```

New code can use [LMOVE](lmove.md) with `RIGHT LEFT`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
RPOPLPUSH demo:queue demo:processing
```
