---
id: dump
sidebar_position: 4
title: DUMP
---

:::info[说明]
官方文档：[DUMP](https://redis.io/docs/latest/commands/dump/)。
:::

读取键值的 Redis 序列化数据。

## 语法

```text
DUMP key
```

`VALUE` 是二进制 `byte[]`，不是 GET 返回的文本；使用 `ResultSet.getBytes("VALUE")` 读取。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，BYTES 类型 |

## 示例

```text
SET demo:message hello
DUMP demo:message
```
