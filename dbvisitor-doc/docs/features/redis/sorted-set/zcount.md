---
id: zcount
sidebar_position: 9
title: ZCOUNT
---

:::info[说明]
官方文档：[ZCOUNT](https://redis.io/docs/latest/commands/zcount/)。
:::

按分数范围统计成员数量。

## 语法

```text
ZCOUNT key min max
```

数值边界默认包含端点，`(10` 表示不含 10；`-inf/+inf` 表示无下界/无上界。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZCOUNT demo:ranking 10 20
```
