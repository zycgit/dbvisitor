---
id: zrangestore
sidebar_position: 23
title: ZRANGESTORE
---

:::info[说明]
官方文档：[ZRANGESTORE](https://redis.io/docs/latest/commands/zrangestore/)。
:::

将范围内的成员及分数保存到目标键。

## 语法

```text
ZRANGESTORE destination source start stop [BYSCORE | BYLEX] [REV] [LIMIT offset count]
```

目标键会被结果覆盖；结果为空时移除目标键。

默认按从 0 开始的排名取范围，包含结束位置，负数从末尾计数。`BYSCORE` 按分数，`BYLEX` 按字典序（成员应同分）。`REV` 反向读取，边界也按从大到小填写。`LIMIT offset count` 用于分数或字典序范围。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 结果有序集合中的元素数量。 |

:::caution[注意]
当前 `BYSCORE` 将边界转换为数字，不支持 `(10` 这类排他边界。读取排他范围可使用 [ZRANGEBYSCORE](zrangebyscore.md)；ZRANGESTORE 没有对应的单命令替代。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZRANGESTORE demo:top demo:ranking 0 9 REV
```
