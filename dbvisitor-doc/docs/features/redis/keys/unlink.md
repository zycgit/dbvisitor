---
id: unlink
sidebar_position: 3
title: UNLINK
---

:::info[说明]
官方文档：[UNLINK](https://redis.io/docs/latest/commands/unlink/)。
:::

删除键，将内存回收交给后台处理。

## 语法

```text
UNLINK key [key ...]
```

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 更新计数 | -- | 实际移除的键数量。 |

## 示例

```text
SET demo:message hello
UNLINK demo:message
```
