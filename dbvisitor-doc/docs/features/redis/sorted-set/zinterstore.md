---
id: zinterstore
sidebar_position: 15
title: ZINTERSTORE
---

:::info[说明]
官方文档：[ZINTERSTORE](https://redis.io/docs/latest/commands/zinterstore/)。
:::

将交集和汇总后的分数保存到目标键。

## 语法

```text
ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
```

`numkeys` 必须与后续源键数量一致。

`WEIGHTS` 按键顺序指定权重，个数与源键相同；默认权重 1。`AGGREGATE` 选择求和、最小或最大分数，默认 `SUM`。

目标键会被结果覆盖；结果为空时移除目标键。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 结果有序集合中的元素数量。 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZINTERSTORE demo:common 2 demo:ranking demo:other WEIGHTS 1 2 AGGREGATE SUM
```
