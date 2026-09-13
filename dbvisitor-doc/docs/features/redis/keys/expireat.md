---
id: expireat
sidebar_position: 7
title: EXPIREAT
---

:::info[说明]
官方文档：[EXPIREAT](https://redis.io/docs/latest/commands/expireat/)。
:::

用 Unix 秒时间戳设置过期时刻。

## 语法

```text
EXPIREAT key unix-seconds [NX | XX | GT | LT]
```

`NX` 仅在键没有过期时间时设置；`XX` 仅修改已有过期时间；`GT/LT` 仅延长/缩短。时间不大于当前时刻时，键会立即删除。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果设置了超时时间，则为 1；否则为 0。 |

## 示例

```text
SET demo:message hello
EXPIREAT demo:message 2000000000
```
