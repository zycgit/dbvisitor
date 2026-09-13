---
id: exists
sidebar_position: 5
title: EXISTS
---

:::info[说明]
官方文档：[EXISTS](https://redis.io/docs/latest/commands/exists/)。
:::

统计指定键中存在的数量。

## 语法

```text
EXISTS key [key ...]
```

重复指定同一个已存在键，会重复计数。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | RESULT 字段，LONG 类型 |

## 示例

```text
SET demo:message hello
EXISTS demo:message demo:missing
```
