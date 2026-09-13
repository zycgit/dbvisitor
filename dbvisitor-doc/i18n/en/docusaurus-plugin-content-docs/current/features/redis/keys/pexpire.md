---
id: pexpire
sidebar_position: 9
title: PEXPIRE
---

:::info[Note]
Official reference: [PEXPIRE](https://redis.io/docs/latest/commands/pexpire/).
:::

Set a key lifetime in milliseconds.

## Syntax

```text
PEXPIRE key milliseconds [NX | XX | GT | LT]
```

`NX` sets only absent expirations; `XX` changes existing expirations; `GT/LT` only extend/shorten them. A lifetime of zero or less deletes the key immediately.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if timeout was set; 0 otherwise. |

## Example

```text
SET demo:message hello
PEXPIRE demo:message 60000
```
