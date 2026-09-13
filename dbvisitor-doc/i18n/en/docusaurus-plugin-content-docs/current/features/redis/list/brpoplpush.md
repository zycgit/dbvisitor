---
id: brpoplpush
sidebar_position: 10
title: BRPOPLPUSH
---

:::info[Note]
Official reference: [BRPOPLPUSH](https://redis.io/docs/latest/commands/brpoplpush/).
:::

Wait and move the source tail element to the destination head.

## Syntax

```text
BRPOPLPUSH source destination timeout
```

`timeout` is in seconds. Use a nonnegative integer literal; `0` waits indefinitely. The example inserts elements before waiting.

New code can use [BLMOVE](blmove.md) with `RIGHT LEFT`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
BRPOPLPUSH demo:queue demo:processing 1
```
