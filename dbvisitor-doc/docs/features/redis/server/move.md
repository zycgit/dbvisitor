---
id: move
sidebar_position: 1
title: MOVE
---

:::info[说明]
官方文档：[MOVE](https://redis.io/docs/latest/commands/move/)。
:::

将键移到另一个逻辑数据库。

## 语法

```text
MOVE key database
```

逻辑数据库编号从 0 开始；Redis Cluster 不支持切换多个逻辑数据库。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果 key 被移动，则为 1；未被移动则为 0 |

## 示例

```text
SET demo:message hello
MOVE demo:message 1
```
