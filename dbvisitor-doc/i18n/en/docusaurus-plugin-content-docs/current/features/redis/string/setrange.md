---
id: setrange
sidebar_position: 18
title: SETRANGE
---

:::info[Note]
Official reference: [SETRANGE](https://redis.io/docs/latest/commands/setrange/).
:::

Overwrite a string starting at a byte offset.

## Syntax

```text
SETRANGE key offset value
```

`offset` is zero-based and nonnegative. Gaps beyond the previous length are padded with zero bytes.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Length of the string after modification. |

## Example

```text
SET demo:message hello
SETRANGE demo:message 1 a
```
