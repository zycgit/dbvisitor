---
id: ttl
sidebar_position: 18
title: TTL
---

:::info[说明]
官方文档：[TTL](https://redis.io/docs/latest/commands/ttl/)。
:::

读取键剩余存活秒数。

## 语法

```text
TTL key
```

`-1` 表示没有过期时间，`-2` 表示键不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello EX 60
TTL demo:message
```
