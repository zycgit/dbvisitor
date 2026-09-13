---
id: decrby
sidebar_position: 6
title: DECRBY
---

:::info[说明]
官方文档：[DECRBY](https://redis.io/docs/latest/commands/decrby/)。
:::

按指定整数减少计数。

## 语法

```text
DECRBY key decrement
```

值和增减量必须是有符号 64 位整数；内容不是整数或结果溢出时报错。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，LONG 类型 |

## 示例

```text
SET demo:counter 10
DECRBY demo:counter 3
```
