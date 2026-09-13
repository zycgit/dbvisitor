---
id: getset
sidebar_position: 11
title: GETSET
---

:::info[说明]
官方文档：[GETSET](https://redis.io/docs/latest/commands/getset/)。
:::

写入新值并返回旧值。

## 语法

```text
GETSET key value
```

键不存在时，返回一行，`VALUE` 为 null。

新代码可使用 [SET ... GET](set.md)，并按需要配置过期选项。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
GETSET demo:message world
```
