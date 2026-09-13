---
id: unlink
sidebar_position: 3
title: UNLINK
---

:::info[Note]
Official reference: [UNLINK](https://redis.io/docs/latest/commands/unlink/).
:::

Remove keys and reclaim their memory asynchronously.

## Syntax

```text
UNLINK key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of keys removed. |

## Example

```text
SET demo:message hello
UNLINK demo:message
```
