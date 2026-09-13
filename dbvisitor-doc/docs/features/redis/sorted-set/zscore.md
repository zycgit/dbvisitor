---
id: zscore
sidebar_position: 17
title: ZSCORE
---

:::info[说明]
官方文档：[ZSCORE](https://redis.io/docs/latest/commands/zscore/)。
:::

读取一个成员的分数。

## 语法

```text
ZSCORE key member
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | SCORE 字段，DOUBLE 类型 |

## 示例

```text
ZADD demo:ranking 10 alice
ZSCORE demo:ranking alice
```
