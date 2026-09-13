---
id: strlen
sidebar_position: 19
title: STRLEN
---

:::info[Note]
Official reference: [STRLEN](https://redis.io/docs/latest/commands/strlen/).
:::

Read the byte length of a string.

## Syntax

```text
STRLEN key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello
STRLEN demo:message
```
