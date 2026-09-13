---
id: zrangebyscore
sidebar_position: 22
title: ZRANGEBYSCORE
---

:::info[说明]
官方文档：[ZRANGEBYSCORE](https://redis.io/docs/latest/commands/zrangebyscore/)。
:::

按分数升序读取范围。

## 语法

```text
ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
```

数值边界默认包含端点，`(10` 表示不含 10；`-inf/+inf` 表示无下界/无上界。

`LIMIT offset count` 跳过 offset 个匹配成员后，最多读取 count 个。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGEBYSCORE demo:ranking 10 20 WITHSCORES LIMIT 0 10
```
