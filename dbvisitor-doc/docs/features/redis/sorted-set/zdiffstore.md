---
id: zdiffstore
sidebar_position: 11
title: ZDIFFSTORE
---

:::info[说明]
官方文档：[ZDIFFSTORE](https://redis.io/docs/latest/commands/zdiffstore/)。
:::

将有序集合差集保存到目标键。

## 语法

```text
ZDIFFSTORE destination numkeys key [key ...]
```

`numkeys` 必须与后续源键数量一致。

目标键会被结果覆盖；结果为空时移除目标键。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 结果有序集合中的元素数量。 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZDIFFSTORE demo:diff 2 demo:ranking demo:other
```
