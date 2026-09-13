---
id: object-encoding
sidebar_position: 13
title: OBJECT ENCODING
---

:::info[Note]
Official reference: [OBJECT ENCODING](https://redis.io/docs/latest/commands/object-encoding/).
:::

Read the internal encoding name of a value.

## Syntax

```text
OBJECT ENCODING key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, STRING type |

## Example

```text
SET demo:message hello
OBJECT ENCODING demo:message
```
