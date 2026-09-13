---
id: msetnx
sidebar_position: 14
title: MSETNX
---

:::info[Note]
Official reference: [MSETNX](https://redis.io/docs/latest/commands/msetnx/).
:::

Write all pairs only when none of the target keys exists.

## Syntax

```text
MSETNX key value [key value ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Returns 0 if no keys were set (at least one key already exists); returns the number of keys if all keys were set. |

## Example

```text
MSETNX demo:new-name mali demo:new-city Shanghai
```
