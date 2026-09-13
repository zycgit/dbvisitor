---
id: rpushx
sidebar_position: 18
title: RPUSHX
---

:::info[说明]
官方文档：[RPUSHX](https://redis.io/docs/latest/commands/rpushx/)。
:::

仅在列表存在时从右端插入。

## 语法

```text
RPUSHX key element [element ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | PUSH 操作后列表的长度。 |

## 示例

```text
RPUSH demo:queue first
RPUSHX demo:queue second
```
