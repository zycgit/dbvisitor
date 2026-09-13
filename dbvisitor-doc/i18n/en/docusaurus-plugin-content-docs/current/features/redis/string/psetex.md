---
id: psetex
sidebar_position: 15
title: PSETEX
---

:::info[Note]
Official reference: [PSETEX](https://redis.io/docs/latest/commands/psetex/).
:::

Write a string with an expiration in milliseconds.

## Syntax

```text
PSETEX key milliseconds value
```

The corresponding `EX`, `PX` or `NX` option of [SET](set.md) is also available.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Returns 1 if successful; 0 otherwise. (Success when status is "OK") |

## Example

```text
PSETEX demo:message 60000 hello
```
