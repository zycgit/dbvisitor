---
id: ping
sidebar_position: 4
title: PING
---

:::info[说明]
官方文档：[PING](https://redis.io/docs/latest/commands/ping/)。
:::

检查连接，可回显指定文本。

## 语法

```text
PING [message]
```

不带参数时返回 `PONG`，带参数时返回该文本。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，STRING 类型 |

## 示例

```text
PING
PING hello
```
