---
id: sunion
sidebar_position: 16
title: SUNION
---

:::info[说明]
官方文档：[SUNION](https://redis.io/docs/latest/commands/sunion/)。
:::

读取多个集合的并集。

## 语法

```text
SUNION key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other rust
SUNION demo:tags demo:other
```
