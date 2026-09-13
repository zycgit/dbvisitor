---
id: srandmember
sidebar_position: 13
title: SRANDMEMBER
---

:::info[Note]
Official reference: [SRANDMEMBER](https://redis.io/docs/latest/commands/srandmember/).
:::

Read random set members without removing them.

## Syntax

```text
SRANDMEMBER key [count]
```

Positive `count` returns distinct members; negative counts allow repeats. Omit it for one member.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SRANDMEMBER demo:tags 2
```
