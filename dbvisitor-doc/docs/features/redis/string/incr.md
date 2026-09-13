---
id: incr
sidebar_position: 3
title: INCR
---

:::info[说明]
官方文档：[INCR](https://redis.io/docs/latest/commands/incr/)。
:::

将整数值加一；键不存在时从零开始。

## 语法

```text
INCR key
```

值和增减量必须是有符号 64 位整数；内容不是整数或结果溢出时报错。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，LONG 类型 |

## 示例

```text
SET demo:counter 10
INCR demo:counter
```
