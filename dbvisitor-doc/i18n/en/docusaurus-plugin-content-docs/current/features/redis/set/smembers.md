---
id: smembers
sidebar_position: 10
title: SMEMBERS
---

:::info[Note]
Official reference: [SMEMBERS](https://redis.io/docs/latest/commands/smembers/).
:::

Read all set members.

## Syntax

```text
SMEMBERS key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SMEMBERS demo:tags
```
