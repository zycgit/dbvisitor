---
id: hpexpire
sidebar_position: 6
title: HPEXPIRE
---

:::info[Note]
Official reference: [HPEXPIRE](https://redis.io/docs/latest/commands/hpexpire/).
:::

Set field lifetimes in milliseconds.

## Syntax

```text
HPEXPIRE key milliseconds [NX | XX | GT | LT] FIELDS numfields field [field ...]
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
HPEXPIRE demo:user 60000 FIELDS 1 name
```
