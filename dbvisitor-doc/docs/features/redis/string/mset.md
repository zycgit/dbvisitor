---
id: mset
sidebar_position: 13
title: MSET
---

:::info[说明]
官方文档：[MSET](https://redis.io/docs/latest/commands/mset/)。
:::

在一条命令中写入多个键值对。

## 语法

```text
MSET key value [key value ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 添加的键数量。 |

## 示例

```text
MSET demo:name mali demo:city Shanghai
```
