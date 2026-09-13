---
id: hpexpiretime
sidebar_position: 8
title: HPEXPIRETIME
---

:::info[Note]
Official reference: [HPEXPIRETIME](https://redis.io/docs/latest/commands/hpexpiretime/).
:::

Read field expiration timestamps in milliseconds.

## Syntax

```text
HPEXPIRETIME key FIELDS numfields field [field ...]
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
HPEXPIRETIME demo:user FIELDS 1 name
```
