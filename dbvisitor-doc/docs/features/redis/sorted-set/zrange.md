---
id: zrange
sidebar_position: 20
title: ZRANGE
---

:::info[说明]
官方文档：[ZRANGE](https://redis.io/docs/latest/commands/zrange/)。
:::

按排名、分数或字典序读取范围。

## 语法

```text
ZRANGE key start stop [BYSCORE | BYLEX] [REV] [LIMIT offset count] [WITHSCORES]
```

默认按从 0 开始的排名取范围，包含结束位置，负数从末尾计数。`BYSCORE` 按分数，`BYLEX` 按字典序（成员应同分）。`REV` 反向读取，边界也按从大到小填写。`LIMIT offset count` 用于分数或字典序范围。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

:::caution[注意]
当前 `BYSCORE` 将边界转换为数字，不支持 `(10` 这类排他边界。读取排他范围可使用 [ZRANGEBYSCORE](zrangebyscore.md)；ZRANGESTORE 没有对应的单命令替代。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGE demo:ranking 0 9 WITHSCORES
ZRANGE demo:ranking 10 20 BYSCORE LIMIT 0 10 WITHSCORES
```
