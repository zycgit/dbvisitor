---
id: sismember
sidebar_position: 8
title: SISMEMBER
---

:::info[Note]
Official reference: [SISMEMBER](https://redis.io/docs/latest/commands/sismember/).
:::

Check whether a member belongs to a set.

## Syntax

```text
SISMEMBER key member
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SADD demo:tags java jdbc
SISMEMBER demo:tags java
```
