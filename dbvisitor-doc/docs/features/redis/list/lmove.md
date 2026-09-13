---
id: lmove
sidebar_position: 1
title: LMOVE
---

:::info[说明]
官方文档：[LMOVE](https://redis.io/docs/latest/commands/lmove/)。
:::

从源列表弹出一个元素，并放入目标列表的指定端。

## 语法

```text
LMOVE source destination LEFT|RIGHT LEFT|RIGHT
```

第一个 `LEFT/RIGHT` 指定源列表弹出端，第二个指定目标列表插入端。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
LMOVE demo:queue demo:processing LEFT RIGHT
```
