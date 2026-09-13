---
id: waitaof
sidebar_position: 3
title: WAITAOF
---

:::info[说明]
官方文档：[WAITAOF](https://redis.io/docs/latest/commands/waitaof/)。
:::

等待此前写入持久化到本机或副本的 AOF。

## 语法

```text
WAITAOF numlocal replicas timeout
```

需要 Redis 7.2+。`numlocal` 为 0 或 1，`replicas` 为目标副本数，`timeout` 单位为毫秒；要求本机持久化时应启用 AOF。返回实际完成数量，不回滚此前写入。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | LOCAL 字段，LONG 类型<br/>REPLICAS 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
WAITAOF 1 0 1000
```
