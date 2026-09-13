---
id: linsert
sidebar_position: 12
title: LINSERT
---

:::info[说明]
官方文档：[LINSERT](https://redis.io/docs/latest/commands/linsert/)。
:::

在第一个匹配元素之前或之后插入。

## 语法

```text
LINSERT key BEFORE|AFTER pivot element
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 插入操作后列表的长度，当 key 不存在时返回 0;当未找到 pivot 时返回 -1。 |

## 示例

```text
RPUSH demo:queue first second
LINSERT demo:queue AFTER first middle
```
