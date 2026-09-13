---
id: type
sidebar_position: 25
title: TYPE
---

:::info[说明]
官方文档：[TYPE](https://redis.io/docs/latest/commands/type/)。
:::

读取键对应的数据结构名称。

## 语法

```text
TYPE key
```

键不存在时返回 `none`。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
TYPE demo:message
```
