---
id: lpush
sidebar_position: 15
title: LPUSH
---

:::info[说明]
官方文档：[LPUSH](https://redis.io/docs/latest/commands/lpush/)。
:::

依次从左端插入元素。

## 语法

```text
LPUSH key element [element ...]
```

多个参数依次压入头部，例如 `first second` 在列表头部形成 `second, first`。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | PUSH 操作后列表的长度。 |

## 示例

```text
LPUSH demo:queue first second
```
