---
id: zrevrangebyscore
sidebar_position: 32
title: ZREVRANGEBYSCORE
---

:::info[说明]
官方文档：[ZREVRANGEBYSCORE](https://redis.io/docs/latest/commands/zrevrangebyscore/)。
:::

按分数降序读取范围。

## 语法

```text
ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
```

数值边界默认包含端点，`(10` 表示不含 10；`-inf/+inf` 表示无下界/无上界。

边界顺序为 `max min`；`LIMIT offset count` 跳过 offset 个匹配成员后，最多读取 count 个。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREVRANGEBYSCORE demo:ranking 20 10 WITHSCORES LIMIT 0 10
```
