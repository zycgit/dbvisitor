---
id: bzpopmin
sidebar_position: 6
title: BZPOPMIN
---

:::info[说明]
官方文档：[BZPOPMIN](https://redis.io/docs/latest/commands/bzpopmin/)。
:::

等待成员后，从第一个非空有序集合弹出最低分成员。

## 语法

```text
BZPOPMIN key [key ...] timeout
```

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | KEY 字段，STRING 类型<br/>ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型 |

:::caution[注意]
当前所有源集合为空（或阻塞超时）时可能报错，不会可靠地返回空结果集。需要处理此情况时使用 Redis 客户端。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
BZPOPMIN demo:ranking 1
```
