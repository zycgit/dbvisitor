---
id: hget
sidebar_position: 9
title: HGET
---

:::info[说明]
官方文档：[HGET](https://redis.io/docs/latest/commands/hget/)。
:::

读取一个字段的值。

## 语法

```text
HGET key field
```

字段不存在时，返回一行，`VALUE` 为 null。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，STRING 类型 |

## 示例

```text
HSET demo:user name mali
HGET demo:user name
```
