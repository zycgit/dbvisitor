---
id: getdel
sidebar_position: 8
title: GETDEL
---

:::info[说明]
官方文档：[GETDEL](https://redis.io/docs/latest/commands/getdel/)。
:::

读取字符串并删除键。

## 语法

```text
GETDEL key
```

键不存在时，返回一行，`VALUE` 为 null。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
GETDEL demo:message
```
