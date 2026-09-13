---
id: hmget
sidebar_position: 14
title: HMGET
---

:::info[说明]
官方文档：[HMGET](https://redis.io/docs/latest/commands/hmget/)。
:::

按指定顺序读取多个字段。

## 语法

```text
HMGET key field [field ...]
```

每个字段对应一行，顺序与参数一致；不存在的字段返回 null，不返回字段名列。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | VALUE 字段，STRING 类型 |

## 示例

```text
HSET demo:user name mali age 18
HMGET demo:user age name
```
