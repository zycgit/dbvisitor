---
id: hpexpiretime
sidebar_position: 8
title: HPEXPIRETIME
---

:::info[说明]
官方文档：[HPEXPIRETIME](https://redis.io/docs/latest/commands/hpexpiretime/)。
:::

读取字段的毫秒级过期时间戳。

## 语法

```text
HPEXPIRETIME key FIELDS numfields field [field ...]
```

需要 Redis 7.4+。`numfields` 与后续字段个数一致；每个字段按参数顺序返回一行 `RESULT`。

`-1` 表示没有过期时间，`-2` 表示字段不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | RESULT 字段，LONG 类型 |

## 示例

```text
HSET demo:user name mali
HPEXPIRE demo:user 60000 FIELDS 1 name
HPEXPIRETIME demo:user FIELDS 1 name
```
