---
id: sdiffstore
sidebar_position: 4
title: SDIFFSTORE
---

:::info[Note]
Official reference: [SDIFFSTORE](https://redis.io/docs/latest/commands/sdiffstore/).
:::

Store a set difference at the destination.

## Syntax

```text
SDIFFSTORE destination key [key ...]
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
SDIFFSTORE demo:diff demo:tags demo:other
```
