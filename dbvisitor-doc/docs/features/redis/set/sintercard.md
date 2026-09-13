---
id: sintercard
sidebar_position: 6
title: SINTERCARD
---

:::info[说明]
官方文档：[SINTERCARD](https://redis.io/docs/latest/commands/sintercard/)。
:::

读取交集的元素数量，可限制计数上限。

## 语法

```text
SINTERCARD numkeys key [key ...] [LIMIT limit]
```

`numkeys` 与键个数一致；`LIMIT` 达到上限即可停止计数，`0` 表示不限制。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SADD demo:tags java jdbc
SADD demo:other java
SINTERCARD 2 demo:tags demo:other LIMIT 10
```
