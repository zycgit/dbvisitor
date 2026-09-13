---
id: strlen
sidebar_position: 19
title: STRLEN
---

:::info[说明]
官方文档：[STRLEN](https://redis.io/docs/latest/commands/strlen/)。
:::

读取字符串的字节长度。

## 语法

```text
STRLEN key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
STRLEN demo:message
```
