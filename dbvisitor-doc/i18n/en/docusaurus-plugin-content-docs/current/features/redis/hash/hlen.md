---
id: hlen
sidebar_position: 13
title: HLEN
---

:::info[Note]
Official reference: [HLEN](https://redis.io/docs/latest/commands/hlen/).
:::

Read the number of hash fields.

## Syntax

```text
HLEN key
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
HSET demo:user name mali age 18
HLEN demo:user
```
