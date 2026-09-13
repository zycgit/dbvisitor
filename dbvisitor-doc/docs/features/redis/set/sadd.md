---
id: sadd
sidebar_position: 1
title: SADD
---

:::info[说明]
官方文档：[SADD](https://redis.io/docs/latest/commands/sadd/)。
:::

向集合添加不重复的元素。

## 语法

```text
SADD key member [member ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 添加到集合中的元素数量，不包括集合中已有的元素。 |

## 示例

```text
SADD demo:tags java jdbc
```
