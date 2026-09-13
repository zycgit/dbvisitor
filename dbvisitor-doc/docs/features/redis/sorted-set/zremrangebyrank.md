---
id: zremrangebyrank
sidebar_position: 28
title: ZREMRANGEBYRANK
---

:::info[说明]
官方文档：[ZREMRANGEBYRANK](https://redis.io/docs/latest/commands/zremrangebyrank/)。
:::

删除排名范围内的成员。

## 语法

```text
ZREMRANGEBYRANK key start stop
```

排名从 0 开始，结束位置包含在范围内；负数从末尾计数。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从有序集合中移除的成员数量，不包括不存在的成员。 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREMRANGEBYRANK demo:ranking 0 0
```
