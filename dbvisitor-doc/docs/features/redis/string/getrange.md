---
id: getrange
sidebar_position: 10
title: GETRANGE
---

:::info[说明]
官方文档：[GETRANGE](https://redis.io/docs/latest/commands/getrange/)。
:::

按字节位置读取字符串片段。

## 语法

```text
GETRANGE key start end
```

下标从 0 开始，结束位置包含在范围内；负数从末尾计数，`-1` 表示最后一个字节。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 1 | VALUE 字段，STRING 类型 |

## 示例

```text
SET demo:message hello
GETRANGE demo:message 0 3
```
