---
id: sinterstore
sidebar_position: 7
title: SINTERSTORE
---

:::info[Note]
Official reference: [SINTERSTORE](https://redis.io/docs/latest/commands/sinterstore/).
:::

Store a set intersection at the destination.

## Syntax

```text
SINTERSTORE destination key [key ...]
```

The result overwrites `destination`; an empty result removes the destination key.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| Update count | -- | Number of elements in the resulting set. |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other java
SINTERSTORE demo:common demo:tags demo:other
```
