---
id: brpoplpush
sidebar_position: 10
title: BRPOPLPUSH
---

:::info[说明]
官方文档：[BRPOPLPUSH](https://redis.io/docs/latest/commands/brpoplpush/)。
:::

等待源列表有元素后将尾部元素移到目标列表头部。

## 语法

```text
BRPOPLPUSH source destination timeout
```

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。示例先写入元素，避免进入等待。

新代码可使用 [BLMOVE](blmove.md) 的 `RIGHT LEFT` 形式。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
BRPOPLPUSH demo:queue demo:processing 1
```
