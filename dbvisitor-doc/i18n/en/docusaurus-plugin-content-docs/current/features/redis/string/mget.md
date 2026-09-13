---
id: mget
sidebar_position: 12
title: MGET
---

:::info[Note]
Official reference: [MGET](https://redis.io/docs/latest/commands/mget/).
:::

Read multiple string keys.

## Syntax

```text
MGET key [key ...]
```

Each distinct key produces one row; missing keys have a null `VALUE`. Repeated keys are collapsed to one row.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | KEY field, STRING type<br/>VALUE field, STRING type |

## Example

```text
MSET demo:name mali demo:city Shanghai
MGET demo:name demo:city
```
