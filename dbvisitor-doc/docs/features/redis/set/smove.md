---
id: smove
sidebar_position: 11
title: SMOVE
---

:::info[说明]
官方文档：[SMOVE](https://redis.io/docs/latest/commands/smove/)。
:::

将一个元素从源集合移到目标集合。

## 语法

```text
SMOVE source destination member
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果元素被移动，则为 1；未被移动则为 0 |

## 示例

```text
SADD demo:tags java jdbc
SMOVE demo:tags demo:other java
```
