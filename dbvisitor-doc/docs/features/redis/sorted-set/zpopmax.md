---
id: zpopmax
sidebar_position: 3
title: ZPOPMAX
---

:::info[说明]
官方文档：[ZPOPMAX](https://redis.io/docs/latest/commands/zpopmax/)。
:::

弹出分数最高的成员。

## 语法

```text
ZPOPMAX key [count]
```

省略 `count` 时弹出一个成员；指定时最多弹出 `count` 个成员。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型<br/>SCORE 字段，DOUBLE 类型 |

:::caution[注意]
当前省略 count 且集合为空时可能报错。读取可能为空的集合时显式写 `count`，如示例中的 `1`。
:::

## 示例

```text
ZADD demo:ranking 10 alice 20 bob
ZPOPMAX demo:ranking 1
```
