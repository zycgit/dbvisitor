---
id: lrem
sidebar_position: 20
title: LREM
---

:::info[说明]
官方文档：[LREM](https://redis.io/docs/latest/commands/lrem/)。
:::

按值移除元素。

## 语法

```text
LREM key count element
```

`count > 0` 从头部删除，`count < 0` 从尾部删除，绝对值限制删除数量；`0` 删除全部匹配元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 移除的元素数量。 |

## 示例

```text
RPUSH demo:queue first second first
LREM demo:queue 1 first
```
