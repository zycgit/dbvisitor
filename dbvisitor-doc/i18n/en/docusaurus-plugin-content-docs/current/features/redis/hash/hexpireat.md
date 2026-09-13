---
id: hexpireat
sidebar_position: 4
title: HEXPIREAT
---

:::info[Note]
Official reference: [HEXPIREAT](https://redis.io/docs/latest/commands/hexpireat/).
:::

Set field expiration using a Unix timestamp in seconds.

## Syntax

```text
HEXPIREAT key unix-seconds [NX | XX | GT | LT] FIELDS numfields field [field ...]
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
HEXPIREAT demo:user 2000000000 FIELDS 1 name
```
