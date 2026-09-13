---
id: hdel
sidebar_position: 1
title: HDEL
---

:::info[说明]
官方文档：[HDEL](https://redis.io/docs/latest/commands/hdel/)。
:::

删除散列中的字段。

## 语法

```text
HDEL key field [field ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从散列中删除的字段数，不包括指定但不存在的字段。如果key不存在，则将其作为空散列处理，此命令返回 0 |

## 示例

```text
HSET demo:user name mali age 18
HDEL demo:user age
```
