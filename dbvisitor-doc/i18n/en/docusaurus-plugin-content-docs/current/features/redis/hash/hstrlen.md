---
id: hstrlen
sidebar_position: 23
title: HSTRLEN
---

:::info[Note]
Official reference: [HSTRLEN](https://redis.io/docs/latest/commands/hstrlen/).
:::

Read the byte length of a field value.

## Syntax

```text
HSTRLEN key field
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali
HSTRLEN demo:user name
```
