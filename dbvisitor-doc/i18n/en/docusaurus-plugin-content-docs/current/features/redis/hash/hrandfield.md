---
id: hrandfield
sidebar_position: 21
title: HRANDFIELD
---

:::info[Note]
Official reference: [HRANDFIELD](https://redis.io/docs/latest/commands/hrandfield/).
:::

Read random hash fields, optionally with their values.

## Syntax

```text
HRANDFIELD key [count [WITHVALUES]]
```

Omit `count` for one field. Positive counts return distinct fields; negative counts allow repeats. `WITHVALUES` must follow `count`.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | FIELD field, STRING type<br/>VALUE field, STRING type (when using WITHVALUES) |

## Example

```text
HSET demo:user name mali age 18
HRANDFIELD demo:user 2 WITHVALUES
```
