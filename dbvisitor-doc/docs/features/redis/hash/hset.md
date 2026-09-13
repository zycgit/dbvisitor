---
id: hset
sidebar_position: 15
title: HSET
---

:::info[说明]
官方文档：[HSET](https://redis.io/docs/latest/commands/hset/)。
:::

新增或覆盖字段值。

## 语法

```text
HSET key field value [field value ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 添加的字段数量。 |

## 示例

```text
HSET demo:user name mali age 18
```
