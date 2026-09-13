---
id: smembers
sidebar_position: 10
title: SMEMBERS
---

:::info[说明]
官方文档：[SMEMBERS](https://redis.io/docs/latest/commands/smembers/)。
:::

读取集合的全部元素。

## 语法

```text
SMEMBERS key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SMEMBERS demo:tags
```
