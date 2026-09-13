---
id: rpush
sidebar_position: 17
title: RPUSH
---

:::info[Note]
Official reference: [RPUSH](https://redis.io/docs/latest/commands/rpush/).
:::

Push elements onto the right end in argument order.

## Syntax

```text
RPUSH key element [element ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the list after the PUSH operation. |

## Example

```text
RPUSH demo:queue first second
```
