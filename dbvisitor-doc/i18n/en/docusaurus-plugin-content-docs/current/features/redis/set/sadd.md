---
id: sadd
sidebar_position: 1
title: SADD
---

:::info[Note]
Official reference: [SADD](https://redis.io/docs/latest/commands/sadd/).
:::

Add unique members to a set.

## Syntax

```text
SADD key member [member ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements added to the set, not including elements already present. |

## Example

```text
SADD demo:tags java jdbc
```
