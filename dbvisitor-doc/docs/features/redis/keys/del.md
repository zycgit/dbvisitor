---
id: del
sidebar_position: 2
title: DEL
---

:::info[说明]
官方文档：[DEL](https://redis.io/docs/latest/commands/del/)。
:::

删除一个或多个键。

## 语法

```text
DEL key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 如果移除一个或多个键，则为大于0的整数；如果指定的键都不存在，则为 0 |

## 示例

```text
SET demo:message hello
DEL demo:message
```
