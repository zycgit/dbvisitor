---
id: sdiff
sidebar_position: 3
title: SDIFF
---

:::info[Note]
Official reference: [SDIFF](https://redis.io/docs/latest/commands/sdiff/).
:::

Read members of the first set absent from the other sets.

## Syntax

```text
SDIFF key [key ...]
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other java
SDIFF demo:tags demo:other
```
