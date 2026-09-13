---
id: hstrlen
sidebar_position: 23
title: HSTRLEN
---

:::info[说明]
官方文档：[HSTRLEN](https://redis.io/docs/latest/commands/hstrlen/)。
:::

读取字段值的字节长度。

## 语法

```text
HSTRLEN key field
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
HSET demo:user name mali
HSTRLEN demo:user name
```
