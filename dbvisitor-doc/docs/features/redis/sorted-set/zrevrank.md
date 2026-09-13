---
id: zrevrank
sidebar_position: 25
title: ZREVRANK
---

:::info[说明]
官方文档：[ZREVRANK](https://redis.io/docs/latest/commands/zrevrank/)。
:::

读取成员的降序排名。

## 语法

```text
ZREVRANK key member [WITHSCORE]
```

排名从 0 开始；`WITHSCORE` 同时返回分数（需要 Redis 7.2+）。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | SCORE 字段，DOUBLE 类型（当使用 WITHSCORE 时）<br/> RANK 字段，LONG 类型 |

:::caution[注意]
当前带 `WITHSCORE` 且成员不存在时可能报错。只需排名时可省略此选项，缺失成员的 `RANK` 为 null。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREVRANK demo:ranking bob WITHSCORE
```
