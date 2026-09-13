---
id: msetnx
sidebar_position: 14
title: MSETNX
---

:::info[说明]
官方文档：[MSETNX](https://redis.io/docs/latest/commands/msetnx/)。
:::

仅在所有目标键都不存在时写入全部键值对。

## 语法

```text
MSETNX key value [key value ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果没有设置任何键（至少有一个键已存在），则返回 0；如果所有键都已设置，则返回键数量。 |

## 示例

```text
MSETNX demo:new-name mali demo:new-city Shanghai
```
