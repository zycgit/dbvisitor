---
id: renamenx
sidebar_position: 22
title: RENAMENX
---

:::info[说明]
官方文档：[RENAMENX](https://redis.io/docs/latest/commands/renamenx/)。
:::

仅在目标键不存在时重命名。

## 语法

```text
RENAMENX key newkey
```

源键不存在时报错。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 1 表示键已重命名，0 表示目标键已存在. |

## 示例

```text
SET demo:original hello
RENAMENX demo:original demo:new-name
```
