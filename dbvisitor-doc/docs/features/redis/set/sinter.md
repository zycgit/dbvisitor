---
id: sinter
sidebar_position: 5
title: SINTER
---

:::info[说明]
官方文档：[SINTER](https://redis.io/docs/latest/commands/sinter/)。
:::

读取多个集合共有的元素。

## 语法

```text
SINTER key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other java
SINTER demo:tags demo:other
```
