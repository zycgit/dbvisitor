---
id: spop
sidebar_position: 12
title: SPOP
---

:::info[Note]
Official reference: [SPOP](https://redis.io/docs/latest/commands/spop/).
:::

Remove and return random set members.

## Syntax

```text
SPOP key [count]
```

Omit `count` for one member; otherwise pop at most `count` distinct members.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
SADD demo:tags java jdbc
SPOP demo:tags 1
```
