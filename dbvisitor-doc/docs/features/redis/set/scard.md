---
id: scard
sidebar_position: 2
title: SCARD
---

:::info[说明]
官方文档：[SCARD](https://redis.io/docs/latest/commands/scard/)。
:::

读取集合元素数量。

## 语法

```text
SCARD key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SADD demo:tags java jdbc
SCARD demo:tags
```
