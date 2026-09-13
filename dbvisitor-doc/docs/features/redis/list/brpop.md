---
id: brpop
sidebar_position: 8
title: BRPOP
---

:::info[说明]
官方文档：[BRPOP](https://redis.io/docs/latest/commands/brpop/)。
:::

等待列表有元素后从右端弹出。

## 语法

```text
BRPOP key [key ...] timeout
```

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。示例先写入元素，避免进入等待。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

:::caution[注意]
有数据时返回两行 `ELEMENT`：第一行是键名，第二行是元素，不是同一行的两列。当前空队列超时可能报错；可靠处理超时的消费者应使用 Redis 客户端。
:::

## 示例

```text
RPUSH demo:queue first second
BRPOP demo:queue 1
```
