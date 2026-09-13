---
id: rename
sidebar_position: 21
title: RENAME
---

:::info[Note]
Official reference: [RENAME](https://redis.io/docs/latest/commands/rename/).
:::

Rename a key, overwriting an existing destination.

## Syntax

```text
RENAME key newkey
```

A missing source key causes an error.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 on success; an existing destination is overwritten, and a missing source causes an error |

## Example

```text
SET demo:original hello
RENAME demo:original demo:renamed
```
