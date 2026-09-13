---
id: lset
sidebar_position: 21
title: LSET
---

:::info[Note]
Official reference: [LSET](https://redis.io/docs/latest/commands/lset/).
:::

Replace an element at an index.

## Syntax

```text
LSET key index element
```

Indices start at zero; negative indices count from the end, with `-1` denoting the last element.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if successful, 0 otherwise. (Success when status is "OK") |

## Example

```text
RPUSH demo:queue first second
LSET demo:queue 0 changed
```
