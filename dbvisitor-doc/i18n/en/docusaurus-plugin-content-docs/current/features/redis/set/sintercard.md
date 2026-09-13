---
id: sintercard
sidebar_position: 6
title: SINTERCARD
---

:::info[Note]
Official reference: [SINTERCARD](https://redis.io/docs/latest/commands/sintercard/).
:::

Count intersection members, optionally up to a limit.

## Syntax

```text
SINTERCARD numkeys key [key ...] [LIMIT limit]
```

`numkeys` must match the key count. `LIMIT` stops counting at the threshold; zero means no limit.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | RESULT field, LONG type |

## Example

```text
SADD demo:tags java jdbc
SADD demo:other java
SINTERCARD 2 demo:tags demo:other LIMIT 10
```
