---
id: touch
sidebar_position: 24
title: TOUCH
---

:::info[说明]
官方文档：[TOUCH](https://redis.io/docs/latest/commands/touch/)。
:::

更新键的最近访问记录。

## 语法

```text
TOUCH key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 被 TOUCH 的键的数量。 |

## 示例

```text
SET demo:message hello
TOUCH demo:message
```
