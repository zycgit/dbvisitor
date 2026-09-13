---
id: wait
sidebar_position: 2
title: WAIT
---

:::info[说明]
官方文档：[WAIT](https://redis.io/docs/latest/commands/wait/)。
:::

等待此前写入得到指定数量副本的确认。

## 语法

```text
WAIT replicas timeout
```

`replicas` 为需要确认的副本数；`timeout` 单位为毫秒，`0` 无限等待。返回实际确认数，超时可能少于目标，之前写入不会因此回滚。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | REPLICAS 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
WAIT 1 1000
```
