---
id: pexpire
sidebar_position: 9
title: PEXPIRE
---

:::info[说明]
官方文档：[PEXPIRE](https://redis.io/docs/latest/commands/pexpire/)。
:::

以毫秒为单位设置键的存活时间。

## 语法

```text
PEXPIRE key milliseconds [NX | XX | GT | LT]
```

`NX` 仅在键没有过期时间时设置；`XX` 仅修改已有过期时间；`GT/LT` 仅延长/缩短。有效期不大于 0 时，键会立即删除。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果设置了超时时间，则为 1；否则为 0。 |

## 示例

```text
SET demo:message hello
PEXPIRE demo:message 60000
```
