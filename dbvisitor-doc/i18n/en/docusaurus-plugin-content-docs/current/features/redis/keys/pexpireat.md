---
id: pexpireat
sidebar_position: 10
title: PEXPIREAT
---

:::info[Note]
Official reference: [PEXPIREAT](https://redis.io/docs/latest/commands/pexpireat/).
:::

Set key expiration using a Unix timestamp in milliseconds.

## Syntax

```text
PEXPIREAT key unix-milliseconds [NX | XX | GT | LT]
```

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. An expiration at or before the current time deletes the key immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if timeout was set; 0 otherwise. |

## Example

```text
SET demo:message hello
PEXPIREAT demo:message 2000000000000
```
