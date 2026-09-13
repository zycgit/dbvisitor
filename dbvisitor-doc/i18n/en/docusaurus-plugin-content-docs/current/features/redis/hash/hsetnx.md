---
id: hsetnx
sidebar_position: 17
title: HSETNX
---

:::info[Note]
Official reference: [HSETNX](https://redis.io/docs/latest/commands/hsetnx/).
:::

Write a field only if it does not exist.

## Syntax

```text
HSETNX key field value
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Returns 0 if the field already exists; returns 1 if a new field was created. |

## Example

```text
HSETNX demo:user name mali
```
