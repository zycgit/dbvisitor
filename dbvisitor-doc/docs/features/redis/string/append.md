---
id: append
sidebar_position: 7
title: APPEND
---

:::info[说明]
官方文档：[APPEND](https://redis.io/docs/latest/commands/append/)。
:::

在字符串末尾追加文本。

## 语法

```text
APPEND key value
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
APPEND demo:message world
```
