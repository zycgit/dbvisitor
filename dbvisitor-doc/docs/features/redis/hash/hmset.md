---
id: hmset
sidebar_position: 16
title: HMSET
---

:::info[说明]
官方文档：[HMSET](https://redis.io/docs/latest/commands/hmset/)。
:::

一次写入多个字段值。

## 语法

```text
HMSET key field value [field value ...]
```

新代码可使用 [HSET](hset.md) 写入多个字段；HSET 的更新计数仅统计新增字段。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 写入的不同字段数量，包括被覆盖的字段。 |

## 示例

```text
HMSET demo:user name mali age 18
```
