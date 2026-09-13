---
id: hexpire
sidebar_position: 3
title: HEXPIRE
---

:::info[说明]
官方文档：[HEXPIRE](https://redis.io/docs/latest/commands/hexpire/)。
:::

为字段设置秒级存活时间。

## 语法

```text
HEXPIRE key seconds [NX | XX | GT | LT] FIELDS numfields field [field ...]
```

需要 Redis 7.4+。`numfields` 与后续字段个数一致；每个字段按参数顺序返回一行 `RESULT`。

`NX` 仅为没有过期时间的字段设置；`XX` 仅修改已有过期时间；`GT/LT` 仅延长/缩短。结果 `1` 为设置成功，`0` 为条件不符，`-2` 为字段不存在，`2` 为立即删除。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | RESULT 字段，LONG 类型 |

## 示例

```text
HSET demo:user name mali age 18
HEXPIRE demo:user 60 FIELDS 2 name age
```
