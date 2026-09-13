---
id: getset
sidebar_position: 11
title: GETSET
---

:::info[Note]
Official reference: [GETSET](https://redis.io/docs/latest/commands/getset/).
:::

Replace a string and return its previous value.

## Syntax

```text
GETSET key value
```

A missing key returns one row with a null `VALUE`.

New code can use [SET ... GET](set.md), with expiration options when needed.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
SET demo:message hello
GETSET demo:message world
```
