---
id: hmset
sidebar_position: 16
title: HMSET
---

:::info[Note]
Official reference: [HMSET](https://redis.io/docs/latest/commands/hmset/).
:::

Write multiple hash fields.

## Syntax

```text
HMSET key field value [field value ...]
```

New code can use [HSET](hset.md) for multiple fields; its update count includes only newly added fields.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of distinct fields written, including overwritten fields. |

## Example

```text
HMSET demo:user name mali age 18
```
