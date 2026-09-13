---
id: zrank
sidebar_position: 24
title: ZRANK
---

:::info[Note]
Official reference: [ZRANK](https://redis.io/docs/latest/commands/zrank/).
:::

Read a member's ascending rank.

## Syntax

```text
ZRANK key member [WITHSCORE]
```

Ranks start at zero. `WITHSCORE` also returns the score (requires Redis 7.2+).

## Results

| Return | Rows | Content |
| --- | --- | --- |
| ResultSet | 1 | SCORE field, DOUBLE type (when using WITHSCORE)<br/>RANK field, LONG type |

:::caution[Caution]
With `WITHSCORE`, a missing member can currently cause an error. Omit the option when only the rank is needed; a missing member then has a null `RANK`.
:::

## Example

```text
ZADD demo:ranking 10 alice 20 bob
ZRANK demo:ranking bob WITHSCORE
```
