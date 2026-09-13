---
id: hpersist
sidebar_position: 18
title: HPERSIST
---

:::info[说明]
官方文档：[HPERSIST](https://redis.io/docs/latest/commands/hpersist/)。
:::

移除字段的过期时间。

## 语法

```text
HPERSIST key FIELDS numfields field [field ...]
```

需要 Redis 7.4+。`numfields` 与后续字段个数一致；每个字段按参数顺序返回一行 `RESULT`。

`1` 表示已移除过期时间，`-1` 表示字段本来没有过期时间，`-2` 表示字段不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | RESULT 字段，LONG 类型 |

## 示例

```text
HSET demo:user name mali
HEXPIRE demo:user 60 FIELDS 1 name
HPERSIST demo:user FIELDS 1 name
```
