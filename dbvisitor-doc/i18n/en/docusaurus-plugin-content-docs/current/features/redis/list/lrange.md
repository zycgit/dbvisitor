---
id: lrange
sidebar_position: 19
title: LRANGE
---

:::info[Note]
Official reference: [LRANGE](https://redis.io/docs/latest/commands/lrange/).
:::

Read elements in an index range.

## Syntax

```text
LRANGE key start stop
```

`start/stop` are zero-based and inclusive; `-1` denotes the last element.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | ELEMENT field, STRING type |

## Example

```text
RPUSH demo:queue first second
LRANGE demo:queue 0 1
```
