---
id: zremrangebyscore
sidebar_position: 29
title: ZREMRANGEBYSCORE
---

:::info[说明]
官方文档：[ZREMRANGEBYSCORE](https://redis.io/docs/latest/commands/zremrangebyscore/)。
:::

删除分数范围内的成员。

## 语法

```text
ZREMRANGEBYSCORE key min max
```

数值边界默认包含端点，`(10` 表示不含 10；`-inf/+inf` 表示无下界/无上界。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从有序集合中移除的成员数量，不包括不存在的成员。 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREMRANGEBYSCORE demo:ranking 0 10
```
