---
id: setrange
sidebar_position: 18
title: SETRANGE
---

:::info[说明]
官方文档：[SETRANGE](https://redis.io/docs/latest/commands/setrange/)。
:::

从指定字节位置开始覆盖字符串。

## 语法

```text
SETRANGE key offset value
```

`offset` 从 0 开始且不能为负；超过原长度时，中间补零字节。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 命令修改后字符串的长度。 |

## 示例

```text
SET demo:message hello
SETRANGE demo:message 1 a
```
