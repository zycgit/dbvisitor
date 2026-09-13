---
id: renamenx
sidebar_position: 22
title: RENAMENX
---

:::info[Note]
Official reference: [RENAMENX](https://redis.io/docs/latest/commands/renamenx/).
:::

Rename a key only if the destination does not exist.

## Syntax

```text
RENAMENX key newkey
```

A missing source key causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if key was renamed; 0 if target key already exists. |

## Example

```text
SET demo:original hello
RENAMENX demo:original demo:new-name
```
