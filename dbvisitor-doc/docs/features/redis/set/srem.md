---
id: srem
sidebar_position: 14
title: SREM
---

:::info[说明]
官方文档：[SREM](https://redis.io/docs/latest/commands/srem/)。
:::

删除指定集合元素。

## 语法

```text
SREM key member [member ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从集合中移除的成员数量，不包括不存在的成员。 |

## 示例

```text
SADD demo:tags java jdbc
SREM demo:tags java
```
