---
id: zrem
sidebar_position: 26
title: ZREM
---

:::info[说明]
官方文档：[ZREM](https://redis.io/docs/latest/commands/zrem/)。
:::

删除指定成员。

## 语法

```text
ZREM key member [member ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从有序集合中移除的成员数量，不包括不存在的成员。 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZREM demo:ranking alice
```
