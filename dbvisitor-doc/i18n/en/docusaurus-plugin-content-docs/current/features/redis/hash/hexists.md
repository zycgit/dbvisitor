---
id: hexists
sidebar_position: 2
title: HEXISTS
---

:::info[Note]
Official reference: [HEXISTS](https://redis.io/docs/latest/commands/hexists/).
:::

Check whether a hash field exists.

## Syntax

```text
HEXISTS key field
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, BOOLEAN type |

## Example

```text
HSET demo:user name mali
HEXISTS demo:user name
```
