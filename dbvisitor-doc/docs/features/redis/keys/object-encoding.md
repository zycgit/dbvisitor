---
id: object-encoding
sidebar_position: 13
title: OBJECT ENCODING
---

:::info[说明]
官方文档：[OBJECT ENCODING](https://redis.io/docs/latest/commands/object-encoding/)。
:::

查看键值的内部编码名称。

## 语法

```text
OBJECT ENCODING key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
OBJECT ENCODING demo:message
```
