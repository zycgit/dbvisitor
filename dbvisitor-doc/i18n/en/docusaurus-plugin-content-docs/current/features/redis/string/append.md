---
id: append
sidebar_position: 7
title: APPEND
---

:::info[Note]
Official reference: [APPEND](https://redis.io/docs/latest/commands/append/).
:::

Append text to a string.

## Syntax

```text
APPEND key value
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello
APPEND demo:message world
```
