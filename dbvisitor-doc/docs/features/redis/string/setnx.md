---
id: setnx
sidebar_position: 17
title: SETNX
---

:::info[说明]
官方文档：[SETNX](https://redis.io/docs/latest/commands/setnx/)。
:::

仅在键不存在时写入字符串。

## 语法

```text
SETNX key value
```

也可用 [SET](set.md) 的 `EX`、`PX` 或 `NX` 选项表达。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果键被设置，则为 1，否则为 0 |

## 示例

```text
SETNX demo:new-message hello
```
