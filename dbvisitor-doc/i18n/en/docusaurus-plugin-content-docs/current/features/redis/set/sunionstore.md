---
id: sunionstore
sidebar_position: 17
title: SUNIONSTORE
---

:::info[Note]
Official reference: [SUNIONSTORE](https://redis.io/docs/latest/commands/sunionstore/).
:::

Store a set union at the destination.

## Syntax

```text
SUNIONSTORE destination key [key ...]
```

The result overwrites `destination`; an empty result removes the destination key.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements in the resulting set. |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other rust
SUNIONSTORE demo:all demo:tags demo:other
```
