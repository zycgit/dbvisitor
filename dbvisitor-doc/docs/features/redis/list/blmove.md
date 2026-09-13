---
id: blmove
sidebar_position: 2
title: BLMOVE
---

:::info[说明]
官方文档：[BLMOVE](https://redis.io/docs/latest/commands/blmove/)。
:::

等待源列表有元素后移动到目标列表。

## 语法

```text
BLMOVE source destination LEFT|RIGHT LEFT|RIGHT timeout
```

第一个 `LEFT/RIGHT` 指定源列表弹出端，第二个指定目标列表插入端。

`timeout` 单位为秒；语句中使用非负整数，`0` 表示无限等待。示例先写入元素，避免进入等待。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
BLMOVE demo:queue demo:processing LEFT RIGHT 1
```
