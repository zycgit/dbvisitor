---
id: keys
sidebar_position: 12
title: KEYS
---

:::info[说明]
官方文档：[KEYS](https://redis.io/docs/latest/commands/keys/)。
:::

查找符合模式的键；驱动通过循环 SCAN 完成遍历。

## 语法

```text
KEYS pattern
```

`pattern` 使用 Redis glob 模式，例如 `demo:*`。`fetchSize` 作为每次 SCAN 的 COUNT 提示，`maxRows` 限制累计返回数量；遍历结果不保证顺序，并发变化时可能重复。

## 返回结果

| 返回形式 | 行数 | 内容 |
| --- | --- | --- |
| 结果集 | 多行 | KEY 字段，STRING 类型（提示：驱动会使用 scan 命令替代 keys 命令的调用） |

## 示例

```text
SET demo:message hello
KEYS demo:*
```
