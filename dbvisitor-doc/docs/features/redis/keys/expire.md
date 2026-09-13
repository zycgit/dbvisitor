---
id: expire
sidebar_position: 6
title: EXPIRE
---

:::info[说明]
官方文档：[EXPIRE](https://redis.io/docs/latest/commands/expire/)。
:::

以秒为单位设置键的存活时间。

## 语法

```text
EXPIRE key seconds [NX | XX | GT | LT]
```

`NX` 仅在键没有过期时间时设置；`XX` 仅修改已有过期时间；`GT/LT` 仅延长/缩短。有效期不大于 0 时，键会立即删除。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果设置了超时时间，则为 1；否则为 0。 |

## 示例

```text
SET demo:message hello
EXPIRE demo:message 60
```
