---
id: lpushx
sidebar_position: 16
title: LPUSHX
---

:::info[说明]
官方文档：[LPUSHX](https://redis.io/docs/latest/commands/lpushx/)。
:::

仅在列表存在时从左端插入。

## 语法

```text
LPUSHX key element [element ...]
```

多个参数依次压入头部，例如 `first second` 在列表头部形成 `second, first`。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | PUSH 操作后列表的长度。 |

## 示例

```text
RPUSH demo:queue first
LPUSHX demo:queue second
```
