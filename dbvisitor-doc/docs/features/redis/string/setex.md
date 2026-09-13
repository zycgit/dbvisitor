---
id: setex
sidebar_position: 16
title: SETEX
---

:::info[说明]
官方文档：[SETEX](https://redis.io/docs/latest/commands/setex/)。
:::

写入字符串并设置秒级过期时间。

## 语法

```text
SETEX key seconds value
```

也可用 [SET](set.md) 的 `EX`、`PX` 或 `NX` 选项表达。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果操作成功，则返回 1；否则返回 0。（当状态为 “OK” 时，表示操作成功） |

## 示例

```text
SETEX demo:message 60 hello
```
