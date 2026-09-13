---
id: hpexpireat
sidebar_position: 7
title: HPEXPIREAT
---

:::info[Note]
Official reference: [HPEXPIREAT](https://redis.io/docs/latest/commands/hpexpireat/).
:::

Set field expiration using a Unix timestamp in milliseconds.

## Syntax

```text
HPEXPIREAT key unix-milliseconds [NX | XX | GT | LT] FIELDS numfields field [field ...]
```

Requires Redis 7.4+. `numfields` must match the number of following fields. Each field produces a `RESULT` row in argument order.

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. Results: `1` set, `0` condition not met, `-2` field missing, `2` deleted immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali
HPEXPIREAT demo:user 2000000000000 FIELDS 1 name
```
