---
id: blmove
sidebar_position: 2
title: BLMOVE
---

:::info[Note]
Official reference: [BLMOVE](https://redis.io/docs/latest/commands/blmove/).
:::

Wait for an element and move it to another list.

## Syntax

```text
BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout
```

The first `LEFT/RIGHT` selects the source end; the second selects the destination end.

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely. The example inserts elements before waiting.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
BLMOVE demo:queue demo:processing LEFT RIGHT 1
```
