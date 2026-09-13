---
id: pttl
sidebar_position: 19
title: PTTL
---

:::info[说明]
官方文档：[PTTL](https://redis.io/docs/latest/commands/pttl/)。
:::

读取键剩余存活毫秒数。

## 语法

```text
PTTL key
```

`-1` 表示没有过期时间，`-2` 表示键不存在。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello PX 60000
PTTL demo:message
```
