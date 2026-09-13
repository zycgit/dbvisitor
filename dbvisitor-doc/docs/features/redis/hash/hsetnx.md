---
id: hsetnx
sidebar_position: 17
title: HSETNX
---

:::info[说明]
官方文档：[HSETNX](https://redis.io/docs/latest/commands/hsetnx/)。
:::

仅在字段不存在时写入。

## 语法

```text
HSETNX key field value
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果该字段已存在，则返回 0；否则，如果创建了一个新字段，则返回 1。 |

## 示例

```text
HSETNX demo:user name mali
```
