---
id: hkeys
sidebar_position: 12
title: HKEYS
---

:::info[Note]
Official reference: [HKEYS](https://redis.io/docs/latest/commands/hkeys/).
:::

List hash field names.

## Syntax

```text
HKEYS key
```

Field order is not guaranteed. Use HGETALL for field-value pairs rather than pairing separate HKEYS and HVALS results by position.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | FIELD field, STRING type |

## Example

```text
HSET demo:user name mali age 18
HKEYS demo:user
```
