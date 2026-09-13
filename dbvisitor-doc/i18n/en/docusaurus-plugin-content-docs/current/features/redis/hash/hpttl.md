---
id: hpttl
sidebar_position: 20
title: HPTTL
---

:::info[Note]
Official reference: [HPTTL](https://redis.io/docs/latest/commands/hpttl/).
:::

Read remaining field lifetimes in milliseconds.

## Syntax

```text
HPTTL key FIELDS numfields field [field ...]
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
HPEXPIRE demo:user 60000 FIELDS 1 name
HPTTL demo:user FIELDS 1 name
```
