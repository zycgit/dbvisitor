---
id: hexpire
sidebar_position: 3
title: HEXPIRE
---

:::info[Note]
Official reference: [HEXPIRE](https://redis.io/docs/latest/commands/hexpire/).
:::

Set field lifetimes in seconds.

## Syntax

```text
HEXPIRE key seconds [NX | XX | GT | LT] FIELDS numfields field [field ...]
```

Requires Redis 7.4+. `numfields` must match the number of following fields. Each field produces a `RESULT` row in argument order.

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. Results: `1` set, `0` condition not met, `-2` field missing, `2` deleted immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali age 18
HEXPIRE demo:user 60 FIELDS 2 name age
```
