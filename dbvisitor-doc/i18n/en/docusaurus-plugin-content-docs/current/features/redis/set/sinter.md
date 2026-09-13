---
id: sinter
sidebar_position: 5
title: SINTER
---

:::info[Note]
Official reference: [SINTER](https://redis.io/docs/latest/commands/sinter/).
:::

Read members common to all input sets.

## Syntax

```text
SINTER key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other java
SINTER demo:tags demo:other
```
