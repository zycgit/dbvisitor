---
id: get
sidebar_position: 2
title: GET
---

:::info[说明]
官方文档：[GET](https://redis.io/docs/latest/commands/get/)。
:::

读取字符串。

## 语法

```text
GET key
```

键不存在时，返回一行，`VALUE` 为 null。

字符串键按文本读取；将键绑定为 `byte[]` 时，可用 `getBytes()` 读取二进制值。示例见[类型支持](../dbvisitor/types.md)。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE：文本模式为 String，二进制模式为 byte[] |

## 示例

```text
SET demo:message hello
GET demo:message
```
