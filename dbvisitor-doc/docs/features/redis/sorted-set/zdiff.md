---
id: zdiff
sidebar_position: 10
title: ZDIFF
---

:::info[说明]
官方文档：[ZDIFF](https://redis.io/docs/latest/commands/zdiff/)。
:::

读取第一个有序集合与其他集合的差集。

## 语法

```text
ZDIFF numkeys key [key ...] [WITHSCORES]
```

`numkeys` 必须与后续源键数量一致。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZDIFF 2 demo:ranking demo:other WITHSCORES
```
