---
id: hrandfield
sidebar_position: 21
title: HRANDFIELD
---

:::info[说明]
官方文档：[HRANDFIELD](https://redis.io/docs/latest/commands/hrandfield/)。
:::

随机读取字段，可同时读取字段值。

## 语法

```text
HRANDFIELD key [count [WITHVALUES]]
```

不写 `count` 时读取一个字段；正数去重，负数允许重复。`WITHVALUES` 必须跟在 `count` 后面。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | FIELD 字段，STRING 类型<br/>VALUE 字段，STRING 类型（当使用 WITHVALUES 时） |

## 示例

```text
HSET demo:user name mali age 18
HRANDFIELD demo:user 2 WITHVALUES
```
