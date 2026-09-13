---
id: httl
sidebar_position: 19
title: HTTL
---

:::info[Note]
Official reference: [HTTL](https://redis.io/docs/latest/commands/httl/).
:::

Read remaining field lifetimes in seconds.

## Syntax

```text
HTTL key FIELDS numfields field [field ...]
```

Requires Redis 7.4+. `numfields` must match the number of following fields. Each field produces a `RESULT` row in argument order.

`-1` means no expiration; `-2` means the field does not exist.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali
HEXPIRE demo:user 60 FIELDS 1 name
HTTL demo:user FIELDS 1 name
```
