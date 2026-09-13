---
id: zinter
sidebar_position: 13
title: ZINTER
---

:::info[说明]
官方文档：[ZINTER](https://redis.io/docs/latest/commands/zinter/)。
:::

读取交集，并汇总成员分数。

## 语法

```text
ZINTER numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
```

`numkeys` 必须与后续源键数量一致。

`WEIGHTS` 按键顺序指定权重，个数与源键相同；默认权重 1。`AGGREGATE` 选择求和、最小或最大分数，默认 `SUM`。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型（当使用 WITHSCORES 时） |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZINTER 2 demo:ranking demo:other WEIGHTS 1 2 AGGREGATE SUM WITHSCORES
```
