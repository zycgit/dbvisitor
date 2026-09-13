---
id: sismember
sidebar_position: 8
title: SISMEMBER
---

:::info[说明]
官方文档：[SISMEMBER](https://redis.io/docs/latest/commands/sismember/)。
:::

判断一个元素是否属于集合。

## 语法

```text
SISMEMBER key member
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SADD demo:tags java jdbc
SISMEMBER demo:tags java
```
