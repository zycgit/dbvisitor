---
id: scard
sidebar_position: 2
title: SCARD
---

:::info[Note]
Official reference: [SCARD](https://redis.io/docs/latest/commands/scard/).
:::

Read the set cardinality.

## Syntax

```text
SCARD key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SADD demo:tags java jdbc
SCARD demo:tags
```
