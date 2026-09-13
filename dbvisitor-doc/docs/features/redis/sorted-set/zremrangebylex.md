---
id: zremrangebylex
sidebar_position: 27
title: ZREMRANGEBYLEX
---

:::info[说明]
官方文档：[ZREMRANGEBYLEX](https://redis.io/docs/latest/commands/zremrangebylex/)。
:::

删除字典序范围内的成员。

## 语法

```text
ZREMRANGEBYLEX key min max
```

成员须使用相同分数。边界 `[value` 包含该值，`(value` 不包含；`-`、`+` 表示无下界、无上界。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 从有序集合中移除的成员数量，不包括不存在的成员。 |

## 示例

```text
ZADD demo:letters 0 alice 0 bob
ZREMRANGEBYLEX demo:letters [a [b
```
