---
id: sdiff
sidebar_position: 3
title: SDIFF
---

:::info[说明]
官方文档：[SDIFF](https://redis.io/docs/latest/commands/sdiff/)。
:::

读取第一个集合中不在其他集合中的元素。

## 语法

```text
SDIFF key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other java
SDIFF demo:tags demo:other
```
