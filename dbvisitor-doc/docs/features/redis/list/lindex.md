---
id: lindex
sidebar_position: 11
title: LINDEX
---

:::info[说明]
官方文档：[LINDEX](https://redis.io/docs/latest/commands/lindex/)。
:::

按位置读取一个元素。

## 语法

```text
LINDEX key index
```

下标从 0 开始；负数从末尾计数，`-1` 表示最后一个元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | ELEMENT 字段，STRING 类型 |

## 示例

```text
RPUSH demo:queue first second
LINDEX demo:queue 0
```
