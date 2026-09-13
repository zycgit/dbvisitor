---
id: spop
sidebar_position: 12
title: SPOP
---

:::info[说明]
官方文档：[SPOP](https://redis.io/docs/latest/commands/spop/)。
:::

随机弹出并删除集合元素。

## 语法

```text
SPOP key [count]
```

省略 `count` 时弹出一个元素；指定时最多弹出 `count` 个不同元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | ELEMENT 字段，STRING 类型 |

## 示例

```text
SADD demo:tags java jdbc
SPOP demo:tags 1
```
