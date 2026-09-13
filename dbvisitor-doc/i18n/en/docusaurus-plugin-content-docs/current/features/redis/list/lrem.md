---
id: lrem
sidebar_position: 20
title: LREM
---

:::info[Note]
Official reference: [LREM](https://redis.io/docs/latest/commands/lrem/).
:::

Remove occurrences of an element.

## Syntax

```text
LREM key count element
```

Positive `count` removes from the head, negative from the tail, up to its absolute value. Zero removes every matching element.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of removed elements. |

## Example

```text
RPUSH demo:queue first second first
LREM demo:queue 1 first
```
