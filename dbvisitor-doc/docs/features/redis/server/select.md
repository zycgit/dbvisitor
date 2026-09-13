---
id: select
sidebar_position: 6
title: SELECT
---

:::info[说明]
官方文档：[SELECT](https://redis.io/docs/latest/commands/select/)。
:::

切换当前连接的逻辑数据库。

## 语法

```text
SELECT database
```

逻辑数据库编号从 0 开始；Redis Cluster 不支持切换多个逻辑数据库。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 操作成功返回 1，否则抛出异常 |

## 示例

```text
SELECT 1
SELECT 0
```
