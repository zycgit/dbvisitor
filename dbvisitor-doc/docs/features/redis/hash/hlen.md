---
id: hlen
sidebar_position: 13
title: HLEN
---

:::info[说明]
官方文档：[HLEN](https://redis.io/docs/latest/commands/hlen/)。
:::

读取字段数量。

## 语法

```text
HLEN key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
HSET demo:user name mali age 18
HLEN demo:user
```
