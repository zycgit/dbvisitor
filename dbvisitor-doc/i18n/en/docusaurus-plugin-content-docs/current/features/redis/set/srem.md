---
id: srem
sidebar_position: 14
title: SREM
---

:::info[Note]
Official reference: [SREM](https://redis.io/docs/latest/commands/srem/).
:::

Remove specified set members.

## Syntax

```text
SREM key member [member ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of members removed from the set, not including non-existing members. |

## Example

```text
SADD demo:tags java jdbc
SREM demo:tags java
```
