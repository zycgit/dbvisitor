---
id: expire
sidebar_position: 6
title: EXPIRE
---

:::info[Note]
Official reference: [EXPIRE](https://redis.io/docs/latest/commands/expire/).
:::

Set a key lifetime in seconds.

## Syntax

```text
EXPIRE key seconds [NX | XX | GT | LT]
```

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. A lifetime of zero or less deletes the key immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if timeout was set; 0 otherwise. |

## Example

```text
SET demo:message hello
EXPIRE demo:message 60
```
