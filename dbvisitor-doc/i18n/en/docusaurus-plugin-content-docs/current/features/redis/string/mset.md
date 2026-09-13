---
id: mset
sidebar_position: 13
title: MSET
---

:::info[Note]
Official reference: [MSET](https://redis.io/docs/latest/commands/mset/).
:::

Write multiple key-value pairs in one command.

## Syntax

```text
MSET key value [key value ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of keys added. |

## Example

```text
MSET demo:name mali demo:city Shanghai
```
