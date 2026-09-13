---
id: expiretime
sidebar_position: 8
title: EXPIRETIME
---

:::info[说明]
官方文档：[EXPIRETIME](https://redis.io/docs/latest/commands/expiretime/)。
:::

读取键的秒级过期时间戳。

## 语法

```text
EXPIRETIME key
```

`-1` 表示没有过期时间，`-2` 表示键不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello EX 60
EXPIRETIME demo:message
```
