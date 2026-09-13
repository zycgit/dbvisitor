---
id: object-refcount
sidebar_position: 16
title: OBJECT REFCOUNT
---

:::info[Note]
Official reference: [OBJECT REFCOUNT](https://redis.io/docs/latest/commands/object-refcount/).
:::

Read the value object's reference count.

## Syntax

```text
OBJECT REFCOUNT key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SET demo:message hello
OBJECT REFCOUNT demo:message
```
