---
id: rpush
sidebar_position: 17
title: RPUSH
---

:::info[说明]
官方文档：[RPUSH](https://redis.io/docs/latest/commands/rpush/)。
:::

依次从右端插入元素。

## 语法

```text
RPUSH key element [element ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | PUSH 操作后列表的长度。 |

## 示例

```text
RPUSH demo:queue first second
```
