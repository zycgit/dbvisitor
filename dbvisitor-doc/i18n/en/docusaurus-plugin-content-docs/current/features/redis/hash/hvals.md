---
id: hvals
sidebar_position: 24
title: HVALS
---

:::info[Note]
Official reference: [HVALS](https://redis.io/docs/latest/commands/hvals/).
:::

Read all hash values.

## Syntax

```text
HVALS key
```

Field order is not guaranteed. Use HGETALL for field-value pairs rather than pairing separate HKEYS and HVALS results by position.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | VALUE field, STRING type |

## Example

```text
HSET demo:user name mali age 18
HVALS demo:user
```
