---
id: del
sidebar_position: 2
title: DEL
---

:::info[Note]
Official reference: [DEL](https://redis.io/docs/latest/commands/del/).
:::

Delete one or more keys.

## Syntax

```text
DEL key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Integer > 0 if one or more keys removed; 0 if none of the specified keys exist |

## Example

```text
SET demo:message hello
DEL demo:message
```
