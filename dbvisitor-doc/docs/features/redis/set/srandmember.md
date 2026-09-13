---
id: srandmember
sidebar_position: 13
title: SRANDMEMBER
---

:::info[说明]
官方文档：[SRANDMEMBER](https://redis.io/docs/latest/commands/srandmember/)。
:::

随机读取集合元素，不删除数据。

## 语法

```text
SRANDMEMBER key [count]
```

正数 `count` 返回不重复元素；负数允许重复；省略时读取一个。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SRANDMEMBER demo:tags 2
```
