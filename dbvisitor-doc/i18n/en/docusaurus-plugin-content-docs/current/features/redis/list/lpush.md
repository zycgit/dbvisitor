---
id: lpush
sidebar_position: 15
title: LPUSH
---

:::info[Note]
Official reference: [LPUSH](https://redis.io/docs/latest/commands/lpush/).
:::

Push elements onto the left end in argument order.

## Syntax

```text
LPUSH key element [element ...]
```

Arguments are pushed onto the head in sequence: `first second` produces `second, first` at the head.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the list after the PUSH operation. |

## Example

```text
LPUSH demo:queue first second
```
