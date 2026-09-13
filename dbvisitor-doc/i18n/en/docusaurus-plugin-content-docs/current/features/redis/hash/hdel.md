---
id: hdel
sidebar_position: 1
title: HDEL
---

:::info[Note]
Official reference: [HDEL](https://redis.io/docs/latest/commands/hdel/).
:::

Remove fields from a hash.

## Syntax

```text
HDEL key field [field ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of fields deleted from the hash, not including non-existing fields. Returns 0 if the key does not exist. |

## Example

```text
HSET demo:user name mali age 18
HDEL demo:user age
```
