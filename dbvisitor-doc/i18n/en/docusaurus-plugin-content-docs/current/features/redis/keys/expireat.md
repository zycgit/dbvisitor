---
id: expireat
sidebar_position: 7
title: EXPIREAT
---

:::info[Note]
Official reference: [EXPIREAT](https://redis.io/docs/latest/commands/expireat/).
:::

Set key expiration using a Unix timestamp in seconds.

## Syntax

```text
EXPIREAT key unix-seconds [NX | XX | GT | LT]
```

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. An expiration at or before the current time deletes the key immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if timeout was set; 0 otherwise. |

## Example

```text
SET demo:message hello
EXPIREAT demo:message 2000000000
```
