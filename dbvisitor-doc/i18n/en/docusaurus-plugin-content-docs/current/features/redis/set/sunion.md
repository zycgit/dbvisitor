---
id: sunion
sidebar_position: 16
title: SUNION
---

:::info[Note]
Official reference: [SUNION](https://redis.io/docs/latest/commands/sunion/).
:::

Read the union of input sets.

## Syntax

```text
SUNION key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other rust
SUNION demo:tags demo:other
```
