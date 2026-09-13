---
id: incrby
sidebar_position: 4
title: INCRBY
---

:::info[说明]
官方文档：[INCRBY](https://redis.io/docs/latest/commands/incrby/)。
:::

按指定整数增加计数。

## 语法

```text
INCRBY key increment
```

值和增减量必须是有符号 64 位整数；内容不是整数或结果溢出时报错。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，LONG 类型 |

## 示例

```text
SET demo:counter 10
INCRBY demo:counter 5
```
