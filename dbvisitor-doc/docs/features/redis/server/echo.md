---
id: echo
sidebar_position: 5
title: ECHO
---

:::info[说明]
官方文档：[ECHO](https://redis.io/docs/latest/commands/echo/)。
:::

回显指定文本。

## 语法

```text
ECHO message
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，STRING 类型 |

## 示例

```text
ECHO hello
```
