---
id: zrevrange
sidebar_position: 30
title: ZREVRANGE
---

:::info[说明]
官方文档：[ZREVRANGE](https://redis.io/docs/latest/commands/zrevrange/)。
:::

按降序排名读取范围。

## 语法

```text
ZREVRANGE key start stop [WITHSCORES]
```

排名从 0 开始，结束位置包含在范围内；负数从末尾计数。

也可使用 [ZRANGE ... REV](zrange.md)。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREVRANGE demo:ranking 0 9 WITHSCORES
```
