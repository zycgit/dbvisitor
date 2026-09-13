---
id: zrevrank
sidebar_position: 25
title: ZREVRANK
---

:::info[Note]
Official reference: [ZREVRANK](https://redis.io/docs/latest/commands/zrevrank/).
:::

Read a member's descending rank.

## Syntax

```text
ZREVRANK key member [WITHSCORE]
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
ZREVRANK demo:ranking bob WITHSCORE
```
