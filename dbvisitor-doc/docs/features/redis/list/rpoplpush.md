---
id: rpoplpush
sidebar_position: 9
title: RPOPLPUSH
---

:::info[说明]
官方文档：[RPOPLPUSH](https://redis.io/docs/latest/commands/rpoplpush/)。
:::

将源列表的尾部元素移到目标列表头部。

## 语法

```text
RPOPLPUSH source destination
```

新代码可使用 [LMOVE](lmove.md) 的 `RIGHT LEFT` 形式。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
RPOPLPUSH demo:queue demo:processing
```
