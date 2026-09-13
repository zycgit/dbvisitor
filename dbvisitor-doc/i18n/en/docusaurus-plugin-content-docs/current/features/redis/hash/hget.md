---
id: hget
sidebar_position: 9
title: HGET
---

:::info[Note]
Official reference: [HGET](https://redis.io/docs/latest/commands/hget/).
:::

Read one hash field.

## Syntax

```text
HGET key field
```

A missing field returns one row with a null `VALUE`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
HSET demo:user name mali
HGET demo:user name
```
