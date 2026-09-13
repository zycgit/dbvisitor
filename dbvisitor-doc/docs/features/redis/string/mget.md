---
id: mget
sidebar_position: 12
title: MGET
---

:::info[说明]
官方文档：[MGET](https://redis.io/docs/latest/commands/mget/)。
:::

读取多个字符串键。

## 语法

```text
MGET key [key ...]
```

每个不同键返回一行，缺失键的 `VALUE` 为 null；重复键会合并为一行。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | KEY 字段，STRING 类型<br/>VALUE 字段，STRING 类型 |

## 示例

```text
MSET demo:name mali demo:city Shanghai
MGET demo:name demo:city
```
