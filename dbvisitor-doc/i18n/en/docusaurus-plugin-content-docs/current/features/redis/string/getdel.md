---
id: getdel
sidebar_position: 8
title: GETDEL
---

:::info[Note]
Official reference: [GETDEL](https://redis.io/docs/latest/commands/getdel/).
:::

Read a string and delete its key.

## Syntax

```text
GETDEL key
```

A missing key returns one row with a null `VALUE`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
SET demo:message hello
GETDEL demo:message
```
