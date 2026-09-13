---
id: setex
sidebar_position: 16
title: SETEX
---

:::info[Note]
Official reference: [SETEX](https://redis.io/docs/latest/commands/setex/).
:::

Write a string with an expiration in seconds.

## Syntax

```text
SETEX key seconds value
```

The corresponding `EX`, `PX` or `NX` option of [SET](set.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Returns 1 if successful; 0 otherwise. (Success when status is "OK") |

## Example

```text
SETEX demo:message 60 hello
```
