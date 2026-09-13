---
id: smove
sidebar_position: 11
title: SMOVE
---

:::info[Note]
Official reference: [SMOVE](https://redis.io/docs/latest/commands/smove/).
:::

Move a member between sets.

## Syntax

```text
SMOVE source destination member
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | 1 if element was moved; 0 otherwise |

## Example

```text
SADD demo:tags java jdbc
SMOVE demo:tags demo:other java
```
