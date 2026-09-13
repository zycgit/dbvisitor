---
id: pexpiretime
sidebar_position: 11
title: PEXPIRETIME
---

:::info[说明]
官方文档：[PEXPIRETIME](https://redis.io/docs/latest/commands/pexpiretime/)。
:::

读取键的毫秒级过期时间戳。

## 语法

```text
PEXPIRETIME key
```

`-1` 表示没有过期时间，`-2` 表示键不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello EX 60
PEXPIRETIME demo:message
```
