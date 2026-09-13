---
id: zlexcount
sidebar_position: 16
title: ZLEXCOUNT
---

:::info[说明]
官方文档：[ZLEXCOUNT](https://redis.io/docs/latest/commands/zlexcount/)。
:::

按字典序范围统计成员数量。

## 语法

```text
ZLEXCOUNT key min max
```

成员须使用相同分数。边界 `[value` 包含该值，`(value` 不包含；`-`、`+` 表示无下界、无上界。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
ZADD demo:letters 0 alice 0 bob
ZLEXCOUNT demo:letters [a [z
```
