---
id: zrevrangebylex
sidebar_position: 31
title: ZREVRANGEBYLEX
---

:::info[说明]
官方文档：[ZREVRANGEBYLEX](https://redis.io/docs/latest/commands/zrevrangebylex/)。
:::

按字典序降序读取范围。

## 语法

```text
ZREVRANGEBYLEX key max min [LIMIT offset count]
```

成员须使用相同分数。边界 `[value` 包含该值，`(value` 不包含；`-`、`+` 表示无下界、无上界。

边界顺序为 `max min`；`LIMIT offset count` 跳过 offset 个匹配成员后，最多读取 count 个。

也可使用 [ZRANGE ... BYLEX REV](zrange.md)。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
ZADD demo:letters 0 alice 0 bob
ZREVRANGEBYLEX demo:letters [z [a LIMIT 0 10
```
