---
id: zscore
sidebar_position: 17
title: ZSCORE
---

:::info[Note]
Official reference: [ZSCORE](https://redis.io/docs/latest/commands/zscore/).
:::

Read one member's score.

## Syntax

```text
ZSCORE key member
```

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | SCORE field, DOUBLE type |

## Example

```text
ZADD demo:ranking 10 alice
ZSCORE demo:ranking alice
```
