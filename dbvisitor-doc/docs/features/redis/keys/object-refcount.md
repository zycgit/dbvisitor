---
id: object-refcount
sidebar_position: 16
title: OBJECT REFCOUNT
---

:::info[说明]
官方文档：[OBJECT REFCOUNT](https://redis.io/docs/latest/commands/object-refcount/)。
:::

查看键值对象的引用计数。

## 语法

```text
OBJECT REFCOUNT key
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
OBJECT REFCOUNT demo:message
```
