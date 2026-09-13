---
id: getex
sidebar_position: 9
title: GETEX
---

:::info[Note]
Official reference: [GETEX](https://redis.io/docs/latest/commands/getex/).
:::

Read a string while setting or removing its expiration.

## Syntax

```text
GETEX key [EX seconds | PX milliseconds | EXAT unix-seconds | PXAT unix-milliseconds | PERSIST]
```

`EX/PX` set lifetimes in seconds/milliseconds; `EXAT/PXAT` set Unix expiration timestamps; `PERSIST` removes expiration.

A missing key returns one row with a null `VALUE`.

Expiration arguments support 64-bit integers, as literals or bound Java `long` values. `PXAT` accepts Unix millisecond timestamps directly.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | VALUE field, STRING type |

## Example

```text
SET demo:message hello
GETEX demo:message EX 60
```
