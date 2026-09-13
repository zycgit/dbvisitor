---
id: zintercard
sidebar_position: 14
title: ZINTERCARD
---

:::info[Note]
Official reference: [ZINTERCARD](https://redis.io/docs/latest/commands/zintercard/).
:::

Count intersection members, optionally up to a limit.

## Syntax

```text
ZINTERCARD numkeys key [key ...] [LIMIT limit]
```

`numkeys` must match the number of following source keys.

`LIMIT` caps the count; zero means no limit.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZINTERCARD 2 demo:ranking demo:other LIMIT 10
```
