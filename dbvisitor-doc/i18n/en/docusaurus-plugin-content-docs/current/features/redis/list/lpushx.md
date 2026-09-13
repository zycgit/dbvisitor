---
id: lpushx
sidebar_position: 16
title: LPUSHX
---

:::info[Note]
Official reference: [LPUSHX](https://redis.io/docs/latest/commands/lpushx/).
:::

Push onto the left end only if the list exists.

## Syntax

```text
LPUSHX key element [element ...]
```

Arguments are pushed onto the head in sequence: `first second` produces `second, first` at the head.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the list after the PUSH operation. |

## Example

```text
RPUSH demo:queue first
LPUSHX demo:queue second
```
