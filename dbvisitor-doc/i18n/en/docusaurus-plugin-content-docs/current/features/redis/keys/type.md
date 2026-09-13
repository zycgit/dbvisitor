---
id: type
sidebar_position: 25
title: TYPE
---

:::info[Note]
Official reference: [TYPE](https://redis.io/docs/latest/commands/type/).
:::

Read the data-structure name of a key.

## Syntax

```text
TYPE key
```

Returns `none` for a missing key.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, STRING type |

## Example

```text
SET demo:message hello
TYPE demo:message
```
