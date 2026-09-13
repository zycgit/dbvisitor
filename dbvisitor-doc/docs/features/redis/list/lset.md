---
id: lset
sidebar_position: 21
title: LSET
---

:::info[说明]
官方文档：[LSET](https://redis.io/docs/latest/commands/lset/)。
:::

覆盖指定位置的元素。

## 语法

```text
LSET key index element
```

下标从 0 开始；负数从末尾计数，`-1` 表示最后一个元素。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 操作是否成功，成功返回 1 否则返回 0，当状态为 “OK” 时表示成功 |

## 示例

```text
RPUSH demo:queue first second
LSET demo:queue 0 changed
```
