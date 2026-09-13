---
id: hpersist
sidebar_position: 18
title: HPERSIST
---

:::info[Note]
Official reference: [HPERSIST](https://redis.io/docs/latest/commands/hpersist/).
:::

Remove field expiration.

## Syntax

```text
HPERSIST key FIELDS numfields field [field ...]
```

Requires Redis 7.4+. `numfields` must match the number of following fields. Each field produces a `RESULT` row in argument order.

`1` means expiration removed; `-1` means the field had no expiration; `-2` means the field is missing.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali
HEXPIRE demo:user 60 FIELDS 1 name
HPERSIST demo:user FIELDS 1 name
```
