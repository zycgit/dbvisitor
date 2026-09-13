---
id: zintercard
sidebar_position: 14
title: ZINTERCARD
---

:::info[说明]
官方文档：[ZINTERCARD](https://redis.io/docs/latest/commands/zintercard/)。
:::

统计交集成员数量，可设置计数上限。

## 语法

```text
ZINTERCARD numkeys key [key ...] [LIMIT limit]
```

`numkeys` 必须与后续源键数量一致。

`LIMIT` 是计数上限，`0` 表示不限制。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZADD demo:other 30 alice
ZINTERCARD 2 demo:ranking demo:other LIMIT 10
```
