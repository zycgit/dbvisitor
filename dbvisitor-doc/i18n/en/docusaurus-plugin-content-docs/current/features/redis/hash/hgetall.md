---
id: hgetall
sidebar_position: 10
title: HGETALL
---

:::info[Note]
Official reference: [HGETALL](https://redis.io/docs/latest/commands/hgetall/).
:::

Read every field and value of a hash.

## Syntax

```text
HGETALL key
```

Field order is not guaranteed. Use HGETALL for field-value pairs rather than pairing separate HKEYS and HVALS results by position.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | FIELD field, STRING type<br/>VALUE field, STRING type |

## Example

```text
HSET demo:user name mali age 18
HGETALL demo:user
```
