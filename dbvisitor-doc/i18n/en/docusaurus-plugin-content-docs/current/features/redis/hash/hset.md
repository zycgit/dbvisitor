---
id: hset
sidebar_position: 15
title: HSET
---

:::info[Note]
Official reference: [HSET](https://redis.io/docs/latest/commands/hset/).
:::

Add or overwrite hash fields.

## Syntax

```text
HSET key field value [field value ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of fields added. |

## Example

```text
HSET demo:user name mali age 18
```
