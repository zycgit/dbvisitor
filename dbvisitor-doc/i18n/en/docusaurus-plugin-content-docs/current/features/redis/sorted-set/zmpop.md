---
id: zmpop
sidebar_position: 1
title: ZMPOP
---

:::info[Note]
Official reference: [ZMPOP](https://redis.io/docs/latest/commands/zmpop/).
:::

Pop low- or high-score members from the first nonempty sorted set.

## Syntax

```text
ZMPOP numkeys key [key ...] MIN|MAX [COUNT count]
```

`numkeys` must match the key count. Pop from the first nonempty key. `MIN/MAX` choose lowest/highest scores; `COUNT` is the maximum to pop, default 1.

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | multiple | KEY field, STRING type<br/>ELEMENT field, STRING type<br/>SCORE field, DOUBLE type |

:::caution[Caution]
When all source sets are empty (or a blocking call times out), the current driver can fail rather than reliably return an empty ResultSet. Use a Redis client when this case must be handled.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZMPOP 1 demo:ranking MIN COUNT 2
```
