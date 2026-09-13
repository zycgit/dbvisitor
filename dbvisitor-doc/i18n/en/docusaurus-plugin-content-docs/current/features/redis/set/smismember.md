---
id: smismember
sidebar_position: 9
title: SMISMEMBER
---

:::info[Note]
Official reference: [SMISMEMBER](https://redis.io/docs/latest/commands/smismember/).
:::

Check membership for several members in order.

## Syntax

```text
SMISMEMBER key member [member ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | RESULT field, BOOLEAN type |

## Example

```text
SADD demo:tags java jdbc
SMISMEMBER demo:tags java rust
```
